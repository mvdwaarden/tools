package nl.ordina.tools.soa.bpel.graph.dm;

public class InvokeNode extends BPELNode {
	private String partnerLink;
	private String operation;
	private String reference;

	public String getPartnerLink() {
		return partnerLink;
	}

	public void setPartnerLink(String partnerLink) {
		this.partnerLink = partnerLink;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

}
