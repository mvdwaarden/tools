package graph.ext.dm.xsd;

import graph.dm.Node;

/**
 * Purpose: A base class for XSD nodes.
 * 
 * @author mwa17610
 * 
 */
public class XSDNode extends Node {
	public XSDNode() {
		super();
	}
	private String namespace;
	private String schemaLocation;
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}
}
