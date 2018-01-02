package nl.ordina.tools.soa.sca.graph.dm;

/**
 * Representation of a SCA wire definition.
 * 
 * A wire connects services and references of a component to services and references in a composite.
 * 
 * @author mwa17610
 *
 */
public class WireNode extends SCANode {
	private String sourceUri;
	private String targetUri;

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(String sourceUri) {
		this.sourceUri = sourceUri;		
	}

	public String getTargetUri() {
		return targetUri;
	}

	public void setTargetUri(String targetUri) {
		this.targetUri = targetUri;		
	}

}
