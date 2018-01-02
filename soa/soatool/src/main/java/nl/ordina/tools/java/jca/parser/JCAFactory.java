package nl.ordina.tools.java.jca.parser;

import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import graph.dm.Edge;
import graph.ext.WSDLUtil;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.java.jca.JCAConst;
import nl.ordina.tools.java.jca.dm.JCAAdapterNode;
import nl.ordina.tools.java.jca.dm.JCANode;

public class JCAFactory extends GraphHandlerFactory<JCANode, Edge<JCANode>> {
	public JCAFactory(JCASAXHandler handler) {
		super(handler);
	}

	@Override
	public JCANode createNode(String uri, String localName, String qName, Attributes atts) {
		JCANode result = null;
		if (nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_ADAPTER_CONFIG, uri, localName)) {
			result = new JCANode();
			setNodeValuesIfEmpty(result, getRelativeId());
			result.setWsdlLocation(atts.getValue(JCAConst.ATTR_WSDL_LOCATION));
			String wsdlLocation = atts.getValue(JCAConst.ATTR_WSDL_LOCATION);

			result.setWsdl(WSDLUtil.getInstance().createWSDLName(DataUtil.getInstance().getFoldername(getSourceUrl()),
					wsdlLocation));
			result.setAdapter(atts.getValue(JCAConst.ATTR_ADAPTER));
		} else if (nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_CONNECTION_FACTORY, uri, localName)) {
			((JCANode) getCurrentNode()).setLocation(atts.getValue(JCAConst.ATTR_LOCATION));
		} else if (nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_INTERACTION_SPECFICATION, uri, localName)
				|| nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_ACTIVATION_SPECIFICATION, uri, localName)) {
			JCAAdapterNode jan;
			JCANode jn = getRootNode();
			result = jan = new JCAAdapterNode();
			setNodeValuesIfEmpty(result, getRootNode().getId() + DataUtil.PATH_SEPARATOR + jn.getPortType()
					+ DataUtil.PATH_SEPARATOR + jn.getOperation());
			jan.setClassName(atts.getValue(JCAConst.ATTR_CLASS_NAME));
		} else if (nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_ENDPOINT_INTERACTION, uri, localName)
				|| nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_ENDPOINT_ACTIVATION, uri, localName)) {
			((JCANode) getCurrentNode()).setPortType(atts.getValue(JCAConst.ATTR_PORT_TYPE));
			((JCANode) getCurrentNode()).setOperation(atts.getValue(JCAConst.ATTR_OPERATION));
		} else if (pathTest(JCAConst.PATH_INTERATION_SPECIFICATION_PROPERTY)
				|| pathTest(JCAConst.PATH_ACTIVATION_SPECIFICATION_PROPERTY)) {
			if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_DELIVERY_MODE)) {
				((JCAAdapterNode) getCurrentNode()).setDeliveryMode(atts.getValue(JCAConst.ATTR_VALUE));
			} else if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_DESTINATION_NAME)) {
				((JCAAdapterNode) getCurrentNode()).setDestinationName(atts.getValue(JCAConst.ATTR_VALUE));
			} else if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_PAYLOAD_TYPE)) {
				((JCAAdapterNode) getCurrentNode()).setPayloadType(atts.getValue(JCAConst.ATTR_VALUE));
			} else if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_SCHEMA_NAME)) {
				((JCAAdapterNode) getCurrentNode()).setSchemaName(atts.getValue(JCAConst.ATTR_VALUE));
			} else if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_PACKAGE_NAME)) {
				((JCAAdapterNode) getCurrentNode()).setPackageName(atts.getValue(JCAConst.ATTR_VALUE));
			} else if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_PROCEDURE_NAME)) {
				((JCAAdapterNode) getCurrentNode()).setProcedureName(atts.getValue(JCAConst.ATTR_VALUE));
			} else if (atts.getValue(JCAConst.ATTR_NAME).equals(JCAConst.PROP_KEY_SQL_STRING)) {
				((JCAAdapterNode) getCurrentNode()).setSqlString(atts.getValue(JCAConst.ATTR_VALUE));
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public List<Edge<JCANode>> createEdges(JCANode source, JCANode target) {
		List<Edge<JCANode>> result = null;

		result = createList(new Edge[] { new Edge<>(source, target) });

		return result;
	}

	@Override
	public void postNodeCreation(JCANode currentNode, String uri, String localName, String qName, StringBuilder data) {
		// @ToDo in de toekomst als task meegenomen moet worden, zal de JCA
		// adapter moeten worden uitgesplitst
		if (nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_INTERACTION_SPECFICATION, uri, localName)
				|| nodeTest(qName, JCAConst.NS_JCA_META, JCAConst.EL_ACTIVATION_SPECIFICATION, uri, localName)) {
			JCANode jn = getRootNode();
			JCAAdapterNode jan = (JCAAdapterNode) currentNode;
			if (jn.getAdapter().equals(JCAConst.ENUM_ADAPTER_TYPE_JMS) && null != jan.getDestinationName()) {
				currentNode.setId(jan.getDestinationName());
				currentNode.setDescription(currentNode.getId());
				currentNode.setId(currentNode.getId().toLowerCase());
			} else if (jn.getAdapter().equals(JCAConst.ENUM_ADAPTER_TYPE_DATABASE) && null != jan.getPackageName()
					&& null != jan.getProcedureName()) {
				currentNode.setId(((null != jan.getSchemaName()) ? (jan.getSchemaName() + ".") : "")
						+ ((null != jan.getPackageName()) ? (jan.getPackageName() + ".") : "")
						+ ((null != jan.getProcedureName()) ? (jan.getProcedureName()) : ""));
				currentNode.setDescription(currentNode.getId());
				currentNode.setId(currentNode.getId().toLowerCase());
			}
		}
	}
}
