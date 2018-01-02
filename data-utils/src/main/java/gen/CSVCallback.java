package gen;

import java.util.Random;

import csv.CSVData;
import csv.CSVUtil;

public class CSVCallback implements GenerationCallback {
	private CSVData data;
	private String filename;
	private int rowIdx;

	public CSVCallback(String filename, int rowIdx) {
		this.filename = filename;
		this.rowIdx = rowIdx;
	}

	public CSVData getData() {
		if (null == this.data)
			this.data = CSVUtil.getInstance().readFromFile(filename, ';', CSVUtil.Option.FIRST_ROW_CONTAINS_HEADERS);
		return this.data;
	}

	@Override
	public String[] generate(int rowId, int totalRows) {
		String[] result = getData().get(new Random().nextInt(getData().size()));
		if (rowIdx >= 0 && rowIdx <= result.length)
			return new String[] { result[rowIdx] };
		else
			return result;
	}

}
