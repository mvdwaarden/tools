package nl.ordina.tools.soa.sca.graph.dm;

public class CompositeNode extends SCANode {
	private String revision;
	private String compositeLabel;
	private String mode;
	private String state;

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getCompositeLabel() {
		return compositeLabel;
	}

	public void setCompositeLabel(String compositeLabel) {
		this.compositeLabel = compositeLabel;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
