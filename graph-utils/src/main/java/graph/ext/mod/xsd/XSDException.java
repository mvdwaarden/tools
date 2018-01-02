package graph.ext.mod.xsd;


public class XSDException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public XSDException(String message, Exception ex) {
        super(message, ex);
    }

}
