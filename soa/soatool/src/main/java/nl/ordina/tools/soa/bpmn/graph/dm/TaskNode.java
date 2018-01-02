package nl.ordina.tools.soa.bpmn.graph.dm;

public class TaskNode extends BPMNNode {
	public enum Type {
		PROCESS_CALL, SERVICE_CALL, SEND_CALL, RECEIVE_CALL
	};

	private String implementation;
	private String operation;
	private Type type;

	public String getImplementation() {
		return implementation;
	}

	public void setImplementation(String implementation) {
		this.implementation = implementation;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	
}
