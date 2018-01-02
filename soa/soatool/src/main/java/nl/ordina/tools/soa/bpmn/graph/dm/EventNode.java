package nl.ordina.tools.soa.bpmn.graph.dm;

public class EventNode extends BPMNNode {
	public enum Type {
		START_EVENT, END_EVENT;
	}

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
