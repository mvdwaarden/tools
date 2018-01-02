package data;

public class DataException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DataException(String message, Exception ex) {
		super(message, ex);
	}

}