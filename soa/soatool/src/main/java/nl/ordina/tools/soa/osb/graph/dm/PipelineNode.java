package nl.ordina.tools.soa.osb.graph.dm;

public class PipelineNode extends OSBNode implements ErrorHandler {
	private String type;
	private String errorHandler;

	public String getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(String errorHandler) {
		this.errorHandler = errorHandler;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
