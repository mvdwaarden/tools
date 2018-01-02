package graph.ext.dm.wsdl;

/**
 * Purpose: Represents an JCA (Java Connector Architecture) operation node. It
 * is a specialization of an OperationNode.
 * 
 * @author mwa17610
 * 
 */
public class JCAOperationNode extends OperationNode {
	private String destinationName;
	private String payloadType;

	public JCAOperationNode() {
		super();
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public void setPayloadType(String payloadType) {
		this.payloadType = payloadType;
	}

	public String getPayloadType() {
		return payloadType;
	}
}
