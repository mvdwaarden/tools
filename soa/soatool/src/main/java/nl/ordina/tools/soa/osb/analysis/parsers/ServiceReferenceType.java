package nl.ordina.tools.soa.osb.analysis.parsers;

import java.util.ArrayList;
import java.util.List;

import nl.ordina.tools.soa.osb.OSBConst;

import org.xml.sax.Attributes;

import xml.XMLSAXHandler;

public class ServiceReferenceType extends XMLSAXHandler {
	private List<String> referenceTypes = new ArrayList<>();
	private String transportProvider;
	private String bindingType;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) {
		super.startElement(uri, localName, qName, atts);
		if (pathTest("actions/route/service")
				&& nodeTest(qName, OSBConst.NS_OSB_ROUTING, "service", uri,
						localName)) {
			String value = atts.getValue(XSI_NS, XSI_TYPE);
			if (null != value && !value.isEmpty()) {
				referenceTypes.add(value);
			}
		} else if (null == bindingType
				&& pathTest("coreEntry/binding")
				&& nodeTest(qName, OSBConst.NS_OSB_SERVICES, "binding", uri,
						localName)) {
			String value = getXsiType(atts);

			bindingType = value;
			if (null != bindingType && bindingType.isEmpty())
				bindingType = "?";
		}
	}

	public String getXsiType(Attributes atts) {
		return atts.getValue(XSI_NS, XSI_TYPE);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		if (null == transportProvider
				&& pathTest("endpointConfig/provider-id")
				&& nodeTest(qName, OSBConst.NS_OSB_TRANSPORTS, "provider-id",
						uri, localName)) {
			transportProvider = getData();
		}
	}

	public List<String> getServiceReferenceTypes() {
		return this.referenceTypes;
	}

	public String getTransportProvider() {
		return transportProvider;
	}

	public void setTransportProvider(String transportProvider) {
		this.transportProvider = transportProvider;
	}

	public String getBindingType() {
		return bindingType;
	}

	public void setBindingType(String bindingType) {
		this.bindingType = bindingType;
	}

}
