package csv;

public interface CSVReaderCallback {
	void readLine(int linenr, String[] row);
}
