package csv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import data.DataUtil;
import data.EnumUtil;
import data.LogUtil;
import data.StringUtil;
import data.Util;

public class CSVUtil implements Util {
	public enum Option {
		FIRST_ROW_CONTAINS_HEADERS, TRIM_VALUES;
	}

	private static final ThreadLocal<CSVUtil> instance = new ThreadLocal<CSVUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static CSVUtil getInstance() {
		CSVUtil result = instance.get();

		if (null == result) {
			result = new CSVUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * Reads a CSV file into a list of string values
	 * 
	 * @param filename
	 * @param commaToken
	 * 
	 * @return
	 */
	public CSVData readFromFile(String filename, char commaToken, Option... options) {
		CSVData result = new CSVData(filename);
		boolean firstRowContainsHeaders = EnumUtil.getInstance().contains(options, Option.FIRST_ROW_CONTAINS_HEADERS);

		readFromFile(filename, commaToken, (i, row) -> {
			if (i == 0 && firstRowContainsHeaders)
				result.setHeader(row);
			else
				result.add(row);
		});

		return result;
	}

	/**
	 * Reads a CSV file into a list of string values
	 * 
	 * @param filename
	 * @param commaToken
	 * @param callback
	 * @param option
	 * 
	 * @return
	 */
	public void readFromFile(String filename, char commaToken, CSVReaderCallback callback) {
		DataUtil.getInstance().readLinesFromFile(filename, "\"", "\"", (i, line) -> {
			String[] row = StringUtil.getInstance().split(line, String.valueOf(commaToken), "\"", "\"");
			callback.readLine(i, row);
		});

	}

	/**
	 * Reads a CSV file into a list of string values
	 * 
	 * @param filename
	 * @return
	 */
	public CSVData readFromFile(String filename) {
		return readFromFile(filename, ',');
	}

	/**
	 * Makes a CSV line based on row
	 * 
	 * @param row
	 * @param commaToken
	 * @return
	 */
	public String makeLine(String[] row, char commaToken, Option... options) {
		StringBuilder result = new StringBuilder();

		if (null != row) {
			for (String cell : row) {
				if (null != cell && EnumUtil.getInstance().contains(options, Option.TRIM_VALUES))
					cell = cell.trim();
				if (null != cell) {
					if (cell.indexOf(commaToken) >= 0) {
						result.append("\"");
						result.append(cell);
						result.append("\"");
					} else {
						result.append(cell);
					}
				}
				result.append(commaToken);
			}
			StringUtil.getInstance().stripEnd(result, "" + commaToken);
		}
		return result.toString();
	}

	/**
	 * Makes a CSV line based on row, previous rows and the commaToken.
	 * 
	 * The previous rows need to be merged
	 * 
	 * @param row
	 * @param previousrows
	 * @param commaToken
	 * @return
	 */
	public String makeLine(List<String[]> rows, char commaToken, Option... options) {
		String[] newRow = new String[rows.get(rows.size() - 1).length];
		for (int i = 0; i < newRow.length; ++i)
			newRow[i] = "";

		for (String[] row : rows)
			for (int i = 0; i < row.length; ++i)
				if (i < newRow.length && null != row[i])
					newRow[i] += row[i];

		return makeLine(newRow, commaToken, options);
	}

	/**
	 * Write CSV data to a stream
	 * 
	 * @param filename
	 * @param csvData
	 * @param commaToken
	 */
	public void writeToStream(OutputStream output, CSVData csvData, CSVWriterCallback cbWriter, Option... options) {

		try (Writer wri = new OutputStreamWriter(output)) {
			String tmp = cbWriter.getHeader(csvData.getHeader(), options);
			if (null != tmp && !tmp.isEmpty())
				wri.write(tmp);
			long lineIdx = 0;
			for (String[] line : csvData.getLines()) {
				tmp = cbWriter.getLine(lineIdx++, line, options);
				if (null != tmp && !tmp.isEmpty())
					wri.write(tmp);
			}
			tmp = cbWriter.getFooter();
			if (null != tmp && !tmp.isEmpty())
				wri.write(tmp);
		} catch (IOException e) {
			LogUtil.getInstance().error("Could not write CSV to OutputStream", e);
		}
	}

	/**
	 * Write CSV data to a CSV stream
	 * 
	 * @param filename
	 * @param csvData
	 * @param commaToken
	 */
	public void writeToCSVStream(OutputStream output, CSVData csvData, char commaToken, Option... options) {
		writeToStream(output, csvData, new CSVWriterCallback() {
			@Override
			public String getHeader(String[] header, Option... options) {
				return makeLine(header, commaToken, options) + "\n";
			}

			@Override
			public String getLine(long lineIdx, String[] row, Option... options) {
				return makeLine(row, commaToken, options) + "\n";
			}

			@Override
			public String getFooter() {
				return null;
			}
		}, options);
	}

	/**
	 * Write CSV data to a file
	 * 
	 * @param filename
	 * @param csvData
	 * @param commaToken
	 * @param options
	 */
	public void writeToFile(String filename, CSVData csvData, char commaToken, Option... options) {
		try (OutputStream os = new FileOutputStream(filename)) {
			writeToCSVStream(os, csvData, commaToken);
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to create file [" + filename + "]");
		}
	}

	/**
	 * Fill empty cells until first from row above
	 */
	public void groupFillEmptyCells(String inputfile, String outputfile, char commaToken, Option... options) {
		class Locals {
			String[] previousrow;
			Writer os;
		}
		;
		Locals _locals = new Locals();
		try {
			_locals.os = new OutputStreamWriter(new FileOutputStream(outputfile));
			readFromFile(inputfile, commaToken, (line, row) -> {
				if (null != _locals.previousrow) {
					for (int i = 0; i < row.length; ++i) {
						if (null == row[i] || row[i].isEmpty() && i >= 0 && i < _locals.previousrow.length) {
							row[i] = _locals.previousrow[i];
						}
					}
				}
				try {
					_locals.os.write(makeLine(row, commaToken) + "\n");
				} catch (IOException e) {
					LogUtil.getInstance().error("Problem writing line to [" + outputfile + "]", e);
				}
				_locals.previousrow = row;

			});
		} catch (FileNotFoundException e) {
			LogUtil.getInstance().error("Could not write CSV to [" + outputfile + "]", e);
		} finally {
			DataUtil.getInstance().close(_locals.os, outputfile);
		}

	}

	/**
	 * Merge rows until an a nonempty cell at index checkNonEmptyCol is found in
	 * a row
	 */
	public void concatShiftUp(String inputfile, String outputfile, char commaToken, int checkNonEmptyCol,
			Option... options) {
		class Locals {
			List<String[]> previousrows = new ArrayList<>();
			Writer os;
		}
		;
		Locals _locals = new Locals();
		try {
			_locals.os = new OutputStreamWriter(new FileOutputStream(outputfile));
			readFromFile(inputfile, commaToken, (line, row) -> {
				_locals.previousrows.add(row);
				if (null != row[checkNonEmptyCol] && !row[checkNonEmptyCol].isEmpty()) {
					try {
						_locals.os.write(makeLine(_locals.previousrows, commaToken) + "\n");
					} catch (IOException e) {
						LogUtil.getInstance().error("Problem writing line to [" + outputfile + "]", e);
					}
					_locals.previousrows = new ArrayList<>();
				}
			});
		} catch (FileNotFoundException e) {
			LogUtil.getInstance().error("Could not write CSV to [" + outputfile + "]", e);
		} finally {
			DataUtil.getInstance().close(_locals.os, outputfile);
		}

	}

	public void copy(String csvFile, char commaToken, String xmlFile, CSVWriterCallback cbWriter, Option... options) {
		boolean hasHeaders = EnumUtil.getInstance().contains(options, Option.FIRST_ROW_CONTAINS_HEADERS);
		try (Writer wri = new BufferedWriter(new FileWriter(xmlFile))) {
			CSVUtil.getInstance().readFromFile(csvFile, commaToken, (idx, line) -> {
				String tmp = null;
				if (hasHeaders && idx == 0) {
					tmp = cbWriter.getHeader(line, options);
				} else {
					tmp = cbWriter.getLine(idx, line, options);
				}
				if (null != tmp && !tmp.isEmpty())
					try {
						wri.write(tmp);
					} catch (IOException e) {
						LogUtil.getInstance()
								.error("Problem converting CSV [" + csvFile + "] to file [" + xmlFile + "]", e);
					}

			});
			wri.write(cbWriter.getFooter());
		} catch (IOException e) {
			LogUtil.getInstance().error("Problem converting CSV [" + csvFile + "] to file [" + xmlFile + "]", e);
		}

	}
}
