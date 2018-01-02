package nl.ordina.tools.wls.graph.dm;

public class ThreadConstraintNode extends ConstraintNode {
	public enum Type {
		MAX_THREAD, MIN_THREAD
	}

	private Type type;

	public ThreadConstraintNode() {
		this.type = Type.MAX_THREAD;
	}

	public ThreadConstraintNode(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
