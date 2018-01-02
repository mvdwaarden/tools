package nl.ordina.tools.soa.sca.graph.dm;

public class Interface extends SCANode {
	public enum Type {
		SERVICE, REFERENCE;
	}

	public enum InteractionType {
		SYNCHRONOUS, ASYNCHRONOUS;
	}

	private String wsdlLocation;
	private String bindingJCA;
	private String wsdlInterface;
	private String wsdlCallbackInterface;
	private Type type;

	public String getWsdlLocation() {
		return wsdlLocation;
	}

	public void setWsdlLocation(String wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}

	public String getWsdlInterface() {
		return wsdlInterface;
	}

	public void setWsdlInterface(String wsdlInterface) {
		this.wsdlInterface = wsdlInterface;
	}

	public String getWsdlCallbackInterface() {
		return wsdlCallbackInterface;
	}

	public void setWsdlCallbackInterface(String wsdlCallbackInterface) {
		this.wsdlCallbackInterface = wsdlCallbackInterface;
	}

	public String getBindingJCA() {
		return bindingJCA;
	}

	public void setBindingJCA(String bindingJCA) {
		this.bindingJCA = bindingJCA;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public InteractionType getInteractionType() {
		return (null != wsdlCallbackInterface && !wsdlCallbackInterface.isEmpty()) ? InteractionType.ASYNCHRONOUS
				: InteractionType.SYNCHRONOUS;
	}

}
