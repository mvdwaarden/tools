package nl.ordina.tools.soa.sca.graph.dm;

public class ComponentNode extends SCANode {
	public enum ImplementationType {
		BPMN, WORKFLOW, DECISION, MEDIATOR, BPEL
	};

	private ImplementationType implementationType;
	private String source;

	public ImplementationType getImplementationType() {
		return implementationType;
	}

	public void setImplementationType(ImplementationType implementationType) {
		this.implementationType = implementationType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
