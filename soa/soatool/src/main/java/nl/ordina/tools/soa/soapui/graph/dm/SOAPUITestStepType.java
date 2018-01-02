package nl.ordina.tools.soa.soapui.graph.dm;

public enum SOAPUITestStepType {
	REQUEST("request");
	private SOAPUITestStepType(String type) {
		this.type = type;
	}

	private String type;

	public String getType() {
		return type;
	}
}
