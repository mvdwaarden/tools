package csv;

public interface CSVWriterCallback {
	String getHeader(String[] header, CSVUtil.Option... option);

	String getLine(long idx, String[] line, CSVUtil.Option... options);

	String getFooter();
}
