package graph.ext.dm.wsdl;

/**
 * Purpose: Represents an schema import node.
 * 
 * @author mwa17610
 * 
 */
public class ImportNode extends WSDLNode {
	private String namespace;
	private String schemaLocation;
    public ImportNode() {
        super();
    }
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
