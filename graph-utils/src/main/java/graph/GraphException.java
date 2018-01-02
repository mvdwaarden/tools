package graph;

public class GraphException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GraphException(String message, Exception ex) {
		super(message, ex);
	}

}
