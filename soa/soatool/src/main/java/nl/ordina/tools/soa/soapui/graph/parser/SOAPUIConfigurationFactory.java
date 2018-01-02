package nl.ordina.tools.soa.soapui.graph.parser;

import java.util.List;

import org.xml.sax.Attributes;

import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.soapui.SOAPUIConst;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUICallNode;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUIConfigurationNode;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUINode;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUITestStepType;
import xml.XMLUtil;

public class SOAPUIConfigurationFactory extends GraphHandlerFactory<SOAPUINode, Edge<SOAPUINode>> {

	public SOAPUIConfigurationFactory(GraphSAXHandler<SOAPUINode, Edge<SOAPUINode>> handler) {
		super(handler);
	}

	@Override
	public SOAPUINode createNode(String uri, String localName, String qName, Attributes atts) {
		SOAPUINode result = null;
		if (nodeTest(qName, uri, localName, SOAPUIConst.NS_SOAPUI_CONFIGURATION, SOAPUIConst.EL_SOAPUI_PROJECT)) {
			result = new SOAPUIConfigurationNode();
			setNodeValuesIfEmpty(result, atts, SOAPUIConst.ATTR_PROJECT_NAME, true);
		} else if (nodeTest(qName, uri, localName, SOAPUIConst.NS_SOAPUI_CONFIGURATION, SOAPUIConst.EL_CALL)
				|| (nodeTest(qName, uri, localName, SOAPUIConst.NS_SOAPUI_CONFIGURATION, SOAPUIConst.EL_TEST_STEP)
						&& SOAPUITestStepType.REQUEST.getType().equals(atts.getValue(SOAPUIConst.ATTR_TYPE)))) {
			result = new SOAPUICallNode();
			setNodeValuesIfEmpty(result, atts, SOAPUIConst.ATTR_CALL_NAME, true);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<SOAPUINode>> createEdges(SOAPUINode source, SOAPUINode target) {
		return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
	}

	@Override
	public void postNodeCreation(SOAPUINode currentNode, String uri, String localName, String qName,
			StringBuilder data) {
		if (currentNode instanceof SOAPUICallNode
				&& nodeTest(qName, uri, localName, SOAPUIConst.NS_SOAPUI_CONFIGURATION, SOAPUIConst.EL_REQUEST)) {
			SOAPUICallNode callNode = (SOAPUICallNode) currentNode;
			String tmp = XMLUtil.getInstance().cvtCDATA2String(data.toString());
			if (null != tmp && tmp.length() > 0)
				callNode.setRequest(tmp);
		}
	}
}
