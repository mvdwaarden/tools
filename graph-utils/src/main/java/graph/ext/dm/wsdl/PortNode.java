package graph.ext.dm.wsdl;

/**
 * Purpose: Represents an port node. A port node links a service node to a binding node.
 * 
 * @author mwa17610
 * 
 */
public class PortNode extends WSDLNode {
	private String binding;

	public String getBindingValue() {
		return binding;
	}

	public void setBindingValue(String binding) {
		this.binding = binding;
	}

}
