package nl.ordina.tools.soa.bpmn.graph.dm;

public class ProcessNode extends BPMNNode {
	public enum Type {
		PROCESS, SUBPROCESS
	};

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
