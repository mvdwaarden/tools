package nl.ordina.tools.soa.osb.graph.dm;

import nl.ordina.tools.soa.osb.OSBConst;

public class ExpressionNode extends OSBNode {
	public enum Type {
		ASSIGN(OSBConst.EL_ASSIGN), REPLACE(OSBConst.EL_REPLACE), INSERT(OSBConst.EL_INSERT);
		private String tag;

		private Type(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

	}

	private long sequence;
	private String variableName;
	private Type type;
	private String queryText;

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setType(String type) {
		for (Type t : Type.values())
			if (t.getTag().equals(type)) {
				this.type = t;
				break;
			}
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getVariableName() {
		return variableName;
	}

	public String getQueryText() {
		return queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}
}
