package gen;

import csv.CSVData;
import data.Util;

/**
 * Purpose: Utility class for graph package
 * 
 * @author mwa17610
 * 
 */
public class DataGenerationUtil implements Util {
	public static final String PATH_SEPARATOR = "/";
	public static final String FILE_ENCODING = "file.encoding";
	public static final String TMP_DIR = "tmp.dir";
	private static final ThreadLocal<DataGenerationUtil> instance = new ThreadLocal<DataGenerationUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static DataGenerationUtil getInstance() {
		DataGenerationUtil result = instance.get();

		if (null == result) {
			result = new DataGenerationUtil();
			instance.set(result);
		}

		return result;
	}

	public CSVData generateData(String[] header, GenerationCallback[] callbacks, int maxRows) {
		CSVData result = new CSVData();
		result.setHeader(header);
		for (int i = 0; i < maxRows; ++i) {
			String[] line = new String[header.length];
			int t = 0;
			for (GenerationCallback cb : callbacks) {
				String[] generated = cb.generate(i, line.length);
				for (String cell : generated)
					line[t++] = cell;
			}
			result.add(line);
		}

		return result;
	}

	public static void main(String[] args) {

	}
}
