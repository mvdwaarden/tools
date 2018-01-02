package nl.ordina.tools.soa.osb.graph.dm;

import java.util.ArrayList;
import java.util.List;

public class BindingNode extends OSBNode {
	private String binding;
	private String providerId;
	private String wsdl;
	private boolean transactionRequired;
	private boolean sameTransactionForResponse;
	private List<String> endpoints;
	private String bindingName;
	private String bindingNamespace;

	public String getBinding() {
		return binding;
	}

	public void setBinding(String binding) {
		this.binding = binding;
	}

	public String getProviderId() {
		return providerId;
	}

	public String getWsdl() {
		return wsdl;
	}

	public void setWsdl(String wsdl) {
		this.wsdl = wsdl;
	}

	public boolean isTransactionRequired() {
		return transactionRequired;
	}

	public void setTransactionRequired(boolean transactionRequired) {
		this.transactionRequired = transactionRequired;
	}

	public boolean isSameTransactionForResponse() {
		return sameTransactionForResponse;
	}

	public void setSameTransactionForResponse(boolean sameTransactionForResponse) {
		this.sameTransactionForResponse = sameTransactionForResponse;
	}

	public void addEndpoint(String endpoint) {
		if (null == endpoints)
			endpoints = new ArrayList<>();
		boolean exists = false;

		for (String e : endpoints) {
			if (e.equalsIgnoreCase(endpoint)) {
				exists = true;
				break;
			}
		}
		if (!exists)
			endpoints.add(endpoint.trim());
	}

	public List<String> getEndpoints() {
		return endpoints;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getBindingName() {
		return bindingName;
	}

	public void setBindingName(String bindingName) {
		this.bindingName = bindingName;
	}

	public String getBindingNamespace() {
		return bindingNamespace;
	}

	public void setBindingNamespace(String bindingNamespace) {
		this.bindingNamespace = bindingNamespace;
	}
	
}
