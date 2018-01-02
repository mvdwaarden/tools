package graph.ext.dm.xsd;

public class XSDComplexTypeNode extends XSDNode {
	private boolean abstractType;
	private String base;
	private boolean extension;

	public boolean isAbstractType() {
		return abstractType;
	}

	public void setAbstractType(boolean abstractType) {
		this.abstractType = abstractType;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public boolean isExtension() {
		return extension;
	}

	public void setExtension(boolean extension) {
		this.extension = extension;
	}
}
