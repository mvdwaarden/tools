package csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import csv.CSVUtil.Option;
import data.Filter;
import data.LogUtil;

public class CSVData {
	private Map<String, CSVHash> hashes = new HashMap<>();
	private CSVColumnHash columnHash;
	private List<String[]> data = new ArrayList<>();
	private String[] header = null;
	private String filename;

	public CSVData(String filename) {
		this.filename = filename;
	}

	public CSVData() {

	}

	public void setHeader(String[] header) {
		this.header = header;
	}

	public void setHeader(List<String> header) {
		this.header = header.toArray(new String[] {});
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String[] getHeader() {
		return this.header;
	}

	public CSVData(List<String[]> data) {
		this.data = data;
	}

	public CSVData append(CSVData data) {
		for (String[] line : data.getLines())
			this.data.add(line);

		return this;
	}

	public CSVData add(String[] line) {
		this.data.add(line);

		return this;
	}

	public CSVData add(List<String> line) {
		this.data.add(line.toArray(new String[] {}));

		return this;
	}

	public CSVData remove(String[] line) {
		this.data.remove(line);

		return this;
	}

	public List<String[]> getLines() {
		return data;
	}

	public String[] get(int idx) {
		return data.get(idx);
	}

	public int size() {
		return data.size();
	}

	public CSVData sort(Comparator<String[]> comparator) {
		Collections.sort(data, comparator);

		return this;
	}

	public List<String[]> filter(Filter<String[]> filter) {
		List<String[]> result = data.stream().filter(f -> filter.include(f)).collect(Collectors.toList());

		return result;
	}

	public CSVData filterSelf(Filter<String[]> filter) {
		data = data.stream().filter(f -> filter.include(f)).collect(Collectors.toList());

		return this;
	}

	public CSVData createHash(String hashname, String[] cols) {
		hashes.put(hashname, new CSVHash(this, hashname, cols));

		return this;
	}

	public String[] get(String hashname, String key) {
		CSVHash hash = hashes.get(hashname);
		if (null == hash) {
			// assume the hash name is the column to build the index for
			hash = new CSVHash(this, hashname, new String[] { hashname });
			hashes.put(hashname, hash);
		}
		return hash.get(key);
	}

	public String get(String hashname, String key, String col) {
		String result = null;
		String[] line = get(hashname, key);

		if (null != line) {
			int idx = getColumnIndex(col);

			if (idx < line.length && idx >= 0)
				result = line[idx];

		}
		return result;
	}

	public int getColumnIndex(String col) {
		Integer result = getColumnHash().get(col);

		return (null == result) ? -1 : result;
	}

	public CSVData clearData() {
		data = new ArrayList<>();
		hashes = new HashMap<>();
		columnHash = null;

		return this;
	}

	public CSVColumnHash getColumnHash() {
		if (null == columnHash && null != getHeader()) {
			columnHash = new CSVColumnHash(getHeader());
		} else if (null == columnHash) {
			LogUtil.getInstance()
					.warning("retrieving column hash, but CSV has no header"
							+ ((null != filename && !filename.isEmpty()) ? ", check if CSV[" + filename
									+ "] is read with [" + Option.FIRST_ROW_CONTAINS_HEADERS + "] option" : ""));
		}

		return columnHash;
	}
}
