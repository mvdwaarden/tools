package nl.ordina.tools.ruleparser;

public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int errorLine;
	private final int errorPosition;

	public ParserException(String message, int errorLine, int errorPosition, Exception ex) {
		super(message, ex);
		this.errorLine = errorLine;
		this.errorPosition = errorPosition;
	}

	public int getErrorLine() {
		return errorLine;
	}

	public int getErrorPosition() {
		return errorPosition;
	}

}
