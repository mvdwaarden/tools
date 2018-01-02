package nl.ordina.tools.soa.osb.graph.dm;

import java.util.ArrayList;
import java.util.List;

public class AlertDestinationNode extends OSBNode {
	private List<String> endpoints;
	private boolean alertToConsole;
	private boolean alertToReporting;
	private boolean alertToSMNP;

	public List<String> getEndpoints() {
		if (null == endpoints) {
			endpoints = new ArrayList<>();
		}
		return endpoints;
	}

	public boolean isAlertToConsole() {
		return alertToConsole;
	}

	public void setAlertToConsole(boolean alertToConsole) {
		this.alertToConsole = alertToConsole;
	}

	public boolean isAlertToReporting() {
		return alertToReporting;
	}

	public void setAlertToReporting(boolean alertToReporting) {
		this.alertToReporting = alertToReporting;
	}

	public boolean isAlertToSMNP() {
		return alertToSMNP;
	}

	public void setAlertToSMNP(boolean alertToSMNP) {
		this.alertToSMNP = alertToSMNP;
	}

	public void addEndpoint(String endpoint) {
		getEndpoints().add(endpoint);
	}

}
