package nl.ordina.tools.soa.osb.graph.dm;

public class ContextNode extends OSBNode implements ErrorHandler {
	private String errorHandler;

	public String getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(String errorHandler) {
		this.errorHandler = errorHandler;
	}
}
