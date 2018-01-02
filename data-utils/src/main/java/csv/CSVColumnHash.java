package csv;

import java.util.HashMap;
import java.util.Map;

public class CSVColumnHash {
	Map<String, Integer> hash;

	public CSVColumnHash(String[] columns) {
		init(columns);
	}

	public void init(String[] columns) {
		hash = new HashMap<>();
		int i = 0;
		for (String col : columns) {
			hash.put(col, i++);
		}
	}

	public Integer get(String col) {
		Integer result = null;
		if (null != hash)
			result = hash.get(col);
		return result;
	}
}
