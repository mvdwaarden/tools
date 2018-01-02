package csv;

import java.util.HashMap;
import java.util.Map;

public class CSVHash {
	String name;
	String[] key;
	Map<String, String[]> hash = new HashMap<>();

	public CSVHash(CSVData data,String name, String[] cols) {
		this.name = name;
		// for each row build the key
		for (String[] row : data.getLines())
			hash.put(getKey(data,cols, row), row);
	}

	public String getKey(CSVData data,String[] cols, String[] row) {
		String result = "";
		for (String col : cols) {
			Integer idx = data.getColumnHash().get(col);
			if (null != idx && row.length > idx) {
				result += row[idx];
			}
		}
		return result;
	}

	public String[] get(String key) {
		return hash.get(key);
	}
}
