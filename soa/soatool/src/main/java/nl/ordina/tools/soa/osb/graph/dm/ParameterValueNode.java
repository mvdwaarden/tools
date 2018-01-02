package nl.ordina.tools.soa.osb.graph.dm;

public class ParameterValueNode extends OSBNode {
	private boolean XMLFragment;
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isXMLFragment() {
		return XMLFragment;
	}

	public void setXMLFragment(boolean xMLFragment) {
		XMLFragment = xMLFragment;
	}

}
