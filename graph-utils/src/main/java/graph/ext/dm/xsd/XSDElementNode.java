package graph.ext.dm.xsd;

public class XSDElementNode extends XSDNode {
	private String type;
	private String ref;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		if (null != ref) {
			this.ref = ref;
		}
	}

}
