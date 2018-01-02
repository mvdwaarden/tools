package graph.ext.dm.wsdl;

/**
 * Purpose: Represents a binding node. A binding nodes links an interface node
 * to a specific transport protocol like, SOAP over http or JMS or what evere.
 * 
 * @author mwa17610
 * 
 */
public class BindingNode extends WSDLNode {
	private String type;
	private String transport;

	public String getTypeValue() {
		return type;
	}

	public void setTypeValue(String type) {
		this.type = type;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

}
