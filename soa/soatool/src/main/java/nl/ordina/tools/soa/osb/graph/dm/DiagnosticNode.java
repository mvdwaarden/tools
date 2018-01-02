package nl.ordina.tools.soa.osb.graph.dm;

public class DiagnosticNode extends OSBNode {
	private String queryText;
	private String severity;
	private String message;

	public String getQueryText() {
		return queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
