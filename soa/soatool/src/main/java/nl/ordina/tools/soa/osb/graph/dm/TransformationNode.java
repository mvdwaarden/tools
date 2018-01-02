package nl.ordina.tools.soa.osb.graph.dm;

import data.EnumUtil;

public class TransformationNode extends OSBNode {
	public enum Type {
		XQUERY, XSLT
	};

	private long size;
	private Type type;

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Type getType() {
		return type;
	}

	public void setType(String type) {
		this.type = (Type) EnumUtil.getInstance().getByName(Type.class, type);
	}

	public void setType(Type type) {
		this.type = type;
	}

}
