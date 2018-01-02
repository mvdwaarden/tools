package nl.ordina.tools.soa.osb.graph.parser;

import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.dm.Node;
import graph.ext.WSDLConst;
import graph.ext.WSDLUtil;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.soa.osb.OSBConst;
import nl.ordina.tools.soa.osb.graph.dm.AlertDestinationNode;
import nl.ordina.tools.soa.osb.graph.dm.AlertNode;
import nl.ordina.tools.soa.osb.graph.dm.BindingNode;
import nl.ordina.tools.soa.osb.graph.dm.BusinessServiceNode;
import nl.ordina.tools.soa.osb.graph.dm.ContextNode;
import nl.ordina.tools.soa.osb.graph.dm.DiagnosticNode;
import nl.ordina.tools.soa.osb.graph.dm.DispatchPolicyNode;
import nl.ordina.tools.soa.osb.graph.dm.ExpressionNode;
import nl.ordina.tools.soa.osb.graph.dm.FlowNode;
import nl.ordina.tools.soa.osb.graph.dm.LoggingKeyNode;
import nl.ordina.tools.soa.osb.graph.dm.LoggingNode;
import nl.ordina.tools.soa.osb.graph.dm.OSBNode;
import nl.ordina.tools.soa.osb.graph.dm.ParameterNode;
import nl.ordina.tools.soa.osb.graph.dm.ParameterValueNode;
import nl.ordina.tools.soa.osb.graph.dm.PipelineEntryNode;
import nl.ordina.tools.soa.osb.graph.dm.PipelineNode;
import nl.ordina.tools.soa.osb.graph.dm.ProxyServiceNode;
import nl.ordina.tools.soa.osb.graph.dm.ReportNode;
import nl.ordina.tools.soa.osb.graph.dm.TransformationNode;
import nl.ordina.tools.soa.osb.graph.dm.VariableNode;
import xml.XMLUtil;

public class OSBFactory extends GraphHandlerFactory<OSBNode, Edge<OSBNode>> {
	private static final String SERVICE_REF_PROXY = "ProxyRef";
	private static final String SERVICE_REF_BUSINESS_SERVICE = "BusinessServiceRef";
	private static final String INVOKE_REF_PIPELINE = "PipelineRef";
	private int expressionSequence;

	public OSBFactory(OSBSAXHandler handler) {
		super(handler);
	}

	@Override
	public OSBNode createNode(String uri, String localName, String qName, Attributes atts) {
		Node currentNode = getCurrentNode();
		OSBNode result = null;
		if (nodeTest(qName, OSBConst.NS_OSB_ROUTING, OSBConst.EL_SERVICE, uri, localName)
				|| nodeTest(qName, OSBConst.NS_OSB_TRANSFORM, OSBConst.EL_SERVICE, uri, localName)
				|| nodeTest(qName, OSBConst.NS_OSB_SERVICES, OSBConst.EL_INVOKE, uri, localName)) {
			String xsiType = getXsiType(atts);
			String ref = atts.getValue(OSBConst.ATTR_REF);
			if (null != ref && !ref.isEmpty()) {
				if (xsiType.endsWith(SERVICE_REF_PROXY)) {
					result = new ProxyServiceNode();
				} else if (xsiType.endsWith(SERVICE_REF_BUSINESS_SERVICE)) {
					result = new BusinessServiceNode();
				} else if (xsiType.endsWith(INVOKE_REF_PIPELINE)) {
					result = new PipelineEntryNode();
				} else {
					result = new OSBNode();
				}
				setOSBNodeValues(result, ref, false);
			}
		} else if (pathTest(OSBConst.PATH_CORE_ENTRY_BINDING_WSDL)
				&& nodeTest(qName, OSBConst.NS_OSB_SERVICES_BINDINGS, OSBConst.EL_WSDL, uri, localName)) {
			if (getCurrentNode() instanceof BindingNode) {
				BindingNode bn = (BindingNode) getCurrentNode();
				bn.setWsdl(WSDLUtil.getInstance().createWSDLName(this.getBaseUrl(),
						atts.getValue(OSBConst.ATTR_REF) + WSDLConst.FILE_EXTENSION_WSDL));
			}
		} else if (nodeTest(qName, null, OSBConst.EL_XML_FRAGMENT, null, localName)) {
			if (getSourceUrl().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_BUSINESS_SERVICE)
					|| getSourceUrl().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_BUSINESS_SERVICE_X))
				result = new BusinessServiceNode();
			else if (getSourceUrl().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_PROXY_SERVICE))
				result = new ProxyServiceNode();
			else if (getSourceUrl().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_PIPELINE))
				result = new PipelineEntryNode();
			else if (getSourceUrl().toLowerCase().endsWith(OSBConst.FILE_EXTENSION_ALERT))
				result = new AlertDestinationNode();
			else
				result = new OSBNode();
			setOSBRootNodeValues(result);
		} else if (nodeTest(qName, OSBConst.NS_OSB_SERVICES, OSBConst.EL_PROXY_SERVICE_ENTRY, uri, localName)) {
			result = new ProxyServiceNode();
			setOSBRootNodeValues(result);
		} else if (nodeTest(qName, OSBConst.NS_OSB_BUSINESS_SERVICE, OSBConst.EL_BUSINESS_SERVICE_ENTRY, uri,
				localName)) {
			result = new BusinessServiceNode();
			setOSBRootNodeValues(result);
		} else if (nodeTest(qName, OSBConst.NS_OSB_PIPELINE, OSBConst.EL_PIPELINE_ENTRY, uri, localName)) {
			result = new PipelineEntryNode();
			setOSBRootNodeValues(result);
		} else if (pathTest(OSBConst.PATH_ROUTER_FLOW)
				&& nodeTest(qName, OSBConst.NS_OSB_PIPELINE, OSBConst.EL_FLOW, uri, localName)) {
			result = new FlowNode();
			setOSBNodeValues(result, null);
		} else if (nodeTest(qName, OSBConst.NS_OSB_PIPELINE, OSBConst.EL_PIPELINE, uri, localName)) {
			PipelineNode pn;
			result = pn = new PipelineNode();
			setOSBNodeValues(result, atts.getValue(OSBConst.ATTR_NAME));
			pn.setType(atts.getValue(OSBConst.ATTR_TYPE));
			pn.setErrorHandler(atts.getValue(OSBConst.ATTR_ERROR_HANDLER));
		} else if (nodeTest(qName, null, OSBConst.EL_DISPATCH_POLICY, null, localName)) {
			// temporary assignment
			result = new DispatchPolicyNode();
		} else if (getCurrentNode() instanceof BindingNode && pathTest(OSBConst.PATH_CORE_ENTRY_BINDING)
				&& nodeTest(qName, OSBConst.NS_OSB_SERVICES, OSBConst.EL_BINDING, uri, localName)) {
			((BindingNode) getCurrentNode()).setBinding(atts.getValue(OSBConst.ATTR_TYPE));
		} else if (getCurrentNode() instanceof BindingNode && pathTest(OSBConst.PATH_CORE_ENTRY_TRANSACTIONS)
				&& nodeTest(qName, OSBConst.NS_OSB_SERVICES, OSBConst.EL_TRANSACTIONS, uri, localName)) {
			((BindingNode) getCurrentNode())
					.setTransactionRequired(Boolean.parseBoolean(atts.getValue(OSBConst.ATTR_TRANSACTION_REQUIRED)));
			((BindingNode) getCurrentNode()).setSameTransactionForResponse(
					Boolean.parseBoolean(atts.getValue(OSBConst.ATTR_SAME_TRANSACTION_FOR_RESPONSE)));
		} else if (nodeTest(qName, OSBConst.NS_OSB_PIPELINE, OSBConst.EL_STAGE, uri, localName)
				|| nodeTest(qName, OSBConst.NS_OSB_PIPELINE, OSBConst.EL_ROUTE_NODE, uri, localName)) {
			ContextNode cn;
			result = cn = new ContextNode();
			setOSBNodeValues(result, atts.getValue(OSBConst.ATTR_NAME));
			cn.setErrorHandler(atts.getValue(OSBConst.ATTR_ERROR_HANDLER));
		} else if (nodeTest(qName, OSBConst.NS_OSB_TYPESYSTEM, OSBConst.EL_VARIABLE, uri, localName)) {
			VariableNode vn;
			result = vn = new VariableNode();
			setOSBNodeValues(result, atts.getValue(OSBConst.ATTR_NAME));
			vn.setPath(atts.getValue(OSBConst.ATTR_PATH));
		} else if ((pathTest(OSBConst.PATH_XQUERY_TRANSFORM_RESOURCE)
				|| pathTest(OSBConst.PATH_XSLT_TRANSFORM_RESOURCE))
				&& nodeTest(qName, null, OSBConst.EL_RESOURCE, null, localName)) {
			TransformationNode tn;
			result = tn = new TransformationNode();
			String ref = atts.getValue(OSBConst.ATTR_REF);
			String filename = getBaseUrl() + DataUtil.PATH_SEPARATOR + ref;
			if (pathTest(OSBConst.PATH_XQUERY_TRANSFORM_RESOURCE)) {
				filename += ".xq";
				tn.setType(TransformationNode.Type.XQUERY);
			} else {
				filename += ".xsl";
				tn.setType(TransformationNode.Type.XSLT);
			}
			tn.setSize(DataUtil.getInstance().getFilesize(filename));
			setNodeValuesIfEmpty(tn, ref);
			tn.setDescription(DataUtil.getInstance().getFilename(ref));
		} else if ((pathTest(OSBConst.PATH_XQUERY_TRANSFORM_PARAMETER)
				|| pathTest(OSBConst.PATH_XSLT_TRANSFORM_PARAMETER))
				&& nodeTest(qName, null, OSBConst.EL_PARAM, null, localName)) {			
			String tmp = atts.getValue(OSBConst.ATTR_NAME);
			result = createParameterNode(tmp);		
		} else if (pathTest(OSBConst.PATH_TRANSFORM_INPUT)
				&& nodeTest(qName, null, OSBConst.EL_INPUT, null, localName)) {
			// temporary assignment
			result = new ParameterValueNode();
		} else if ((pathTest(OSBConst.PATH_PARAMETER_VALUE))
				&& nodeTest(qName, null, OSBConst.EL_PATH, null, localName)) {
			result = new ParameterValueNode();
		} else if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_LOG, uri, localName)) {
			result = new LoggingNode();
		} else if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_REPORT, uri, localName)) {
			result = new ReportNode();
		} else if (nodeTest(qName, OSBConst.NS_OSB_ALERT, OSBConst.EL_ALERT, uri, localName)) {
			result = new AlertNode();
		} else if (nodeTest(qName, OSBConst.NS_OSB_TRANSFORM, OSBConst.EL_ASSIGN, uri, localName)
				|| nodeTest(qName, OSBConst.NS_OSB_TRANSFORM, OSBConst.EL_REPLACE, uri, localName)
				|| nodeTest(qName, OSBConst.NS_OSB_TRANSFORM, OSBConst.EL_INSERT, uri, localName)) {

			String varName = atts.getValue(OSBConst.ATTR_VAR_NAME);

			if (null != varName && !varName.isEmpty()) {
				ExpressionNode e;
				result = e = new ExpressionNode();
				e.setType(localName);
				e.setSequence(++expressionSequence);
				e.setName(e.getType().name());
				e.setVariableName(varName);
				e.setDescription(localName + ((localName.equals(OSBConst.EL_ASSIGN)) ? " to " : " in ") + varName);
			}
		} else if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_LABELS, uri, localName)) {
			result = new LoggingKeyNode();
		} else if (currentNode instanceof AlertNode
				&& nodeTest(qName, OSBConst.NS_OSB_ALERT, OSBConst.EL_DESTINATION, uri, localName)) {
			((AlertNode) currentNode).setDestination(atts.getValue(OSBConst.ATTR_REF));
		}
		return result;
	}

	private void setOSBRootNodeValues(OSBNode result) {
		setOSBNodeValues(result, null);
	}

	private String getOSBName(String name, boolean prepend) {
		return ((prepend) ? DataUtil.getInstance().removeExtension(getRelativeId()) : "")
				+ ((null == name) ? "" : ((prepend) ? DataUtil.PATH_SEPARATOR : "") + name);
	}

	private void setOSBNodeValues(OSBNode result, String name, boolean prepend) {
		/*
		 * opletten, matching gebeurt op basis van pad, relatief aan de OSB
		 * configuration project
		 */
		String osbname = getOSBName(name, prepend);
		setNodeValuesIfEmpty(result, osbname.toLowerCase());
		result.setDescription(DataUtil.getInstance().getFilename(osbname));
	}

	private void setOSBNodeValues(OSBNode result, String name) {
		setOSBNodeValues(result, name, true);
	}

	@Override
	public void postNodeCreation(OSBNode currentNode, String uri, String localName, String qName, StringBuilder data) {
		if (currentNode instanceof DispatchPolicyNode
				&& nodeTest(qName, null, OSBConst.EL_DISPATCH_POLICY, null, localName)) {
			String policy = data.toString();
			currentNode.setId(policy);
			currentNode.setDescription(policy);
			currentNode.setName(policy);
		} else if (currentNode instanceof VariableNode && pathTest(OSBConst.PATH_TRANSFORM_INPUT)
				&& nodeTest(qName, null, OSBConst.EL_INPUT, null, localName)) {
			setOSBNodeValues(currentNode, getVariableName(data.toString()));
		} else if (currentNode instanceof ExpressionNode) {
			ExpressionNode en = (ExpressionNode) currentNode;
			if (pathTest(OSBConst.PATH_REPLACE_ID) || pathTest(OSBConst.PATH_INSERT_ID)
					|| pathTest(OSBConst.PATH_ASSIGN_ID)) {
				currentNode.setId(data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_STAGES, OSBConst.EL_XQUERYTEXT, uri, localName)) {
				en.setQueryText(data.toString());
			}
		} else if ((pathTest(OSBConst.PATH_PORT_NAME) || pathTest(OSBConst.PATH_BINDING_NAME))
				&& currentNode instanceof BindingNode) {
			BindingNode bn = (BindingNode) currentNode;
			bn.setBindingName(data.toString().trim());
		} else if ((pathTest(OSBConst.PATH_BINDING_NAMESPACE) || pathTest(OSBConst.PATH_PORT_NAMESPACE))
				&& currentNode instanceof BindingNode) {
			BindingNode bn = (BindingNode) currentNode;
			bn.setBindingNamespace(data.toString().trim());
		} else if (pathTest(OSBConst.PATH_ENDPOINT_CONFIG_URI_VALUE) && currentNode instanceof BindingNode) {
			BindingNode bn = (BindingNode) currentNode;
			bn.addEndpoint(data.toString().trim());
		} else if (pathTest(OSBConst.PATH_ENDPOINT_PROVIDER_ID) && currentNode instanceof BindingNode) {
			BindingNode bn = (BindingNode) currentNode;
			bn.setProviderId(data.toString().trim());
		} else if (currentNode instanceof DiagnosticNode) {
			if (nodeTest(qName, OSBConst.NS_OSB_STAGES, OSBConst.EL_ID, uri, localName)) {
				currentNode.setId(data.toString());
				if (null == currentNode.getDescription() || currentNode.getDescription().isEmpty())
					currentNode.setDescription(data.toString());
				currentNode.setName(data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_STAGES, OSBConst.EL_XQUERYTEXT, uri, localName)
					&& currentNode instanceof DiagnosticNode) {
				DiagnosticNode dn = (DiagnosticNode) currentNode;
				dn.setQueryText(data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_MESSAGE, uri, localName)
					|| nodeTest(qName, OSBConst.NS_OSB_ALERT, OSBConst.EL_DESCRIPTION, uri, localName)) {
				DiagnosticNode dn = (DiagnosticNode) currentNode;
				dn.setMessage(data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_LOG_LEVEL, uri, localName)
					|| nodeTest(qName, OSBConst.NS_OSB_ALERT, OSBConst.EL_SEVERITY, uri, localName)) {
				DiagnosticNode dn = (DiagnosticNode) currentNode;
				dn.setSeverity(data.toString());
			} else if (nodeTest(qName, null, OSBConst.EL_DESCRIPTION, null, localName)) {
				DiagnosticNode dn = (DiagnosticNode) currentNode;
				if (null == currentNode.getDescription() || currentNode.getDescription().isEmpty())
					dn.setDescription(data.toString());
			}
		} else if (currentNode instanceof LoggingKeyNode) {
			LoggingKeyNode kn = (LoggingKeyNode) currentNode;
			if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_KEY, uri, localName)) {
				setNodeValuesIfEmpty(kn, data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_LOGGING, OSBConst.EL_VARIABLE_NAME, uri, localName)) {
				kn.setVariable(data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_STAGES, OSBConst.EL_XPATH_TEXT, uri, localName)) {
				kn.setXpath(data.toString());
			}
		} else if (currentNode instanceof AlertDestinationNode) {
			AlertDestinationNode adn = (AlertDestinationNode) currentNode;
			if (pathTest(OSBConst.PATH_CONFIG_URI_VALUE)) {
				adn.addEndpoint(data.toString());
			} else if (nodeTest(qName, OSBConst.NS_OSB_MONITORING, OSBConst.EL_ALERT_TO_CONSOLE, uri, localName)) {
				adn.setAlertToConsole(Boolean.parseBoolean(data.toString()));
			} else if (nodeTest(qName, OSBConst.NS_OSB_MONITORING, OSBConst.EL_ALERT_TO_REPORTING_DATASET, uri,
					localName)) {
				adn.setAlertToReporting(Boolean.parseBoolean(data.toString()));
			} else if (nodeTest(qName, OSBConst.NS_OSB_MONITORING, OSBConst.EL_ALERT_TO_SNMP, uri, localName)) {
				adn.setAlertToSMNP(Boolean.parseBoolean(data.toString()));
			}
		} else if (currentNode instanceof ParameterValueNode) {
			ExpressionNode parentExpression = (ExpressionNode) getParentNode(ExpressionNode.class);

			ParameterValueNode pvn = (ParameterValueNode) currentNode;
			String tmp = data.toString();
			if (XMLUtil.getInstance().isXML(tmp)) {
				pvn.setId(parentExpression.getId() + DataUtil.PATH_SEPARATOR + "xml-fragment");
				pvn.setXMLFragment(true);
			} else {
				pvn.setId(tmp);
			}
			pvn.setName(tmp);
			pvn.setDescription(tmp);
			pvn.setValue(tmp);
		}
	}

	private String getVariableName(String name) {
		return DataUtil.getInstance().getFilenameWithoutExtension(getRelativeId()) + name;
	}

	private VariableNode createVariableNode(String name) {
		VariableNode result = new VariableNode();

		setOSBNodeValues(result, name);

		return result;
	}

	private ParameterNode createParameterNode(String name) {
		ExpressionNode parentExpression = (ExpressionNode) getParentNode(ExpressionNode.class);
		ParameterNode result = new ParameterNode();

		setOSBNodeValues(result, name, false);

		result.setId(parentExpression.getId() + DataUtil.PATH_SEPARATOR + result.getDescription());

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<OSBNode>> createEdges(OSBNode source, OSBNode target) {
		if (target instanceof ParameterValueNode && !(source instanceof ParameterNode)) {
			// this is the case for an XSLT which has ONLY one input, so the
			// parameter 'input' is implicit.
			ParameterNode pn = createParameterNode(OSBConst.EL_INPUT);
			return createList(new Edge[] { new Edge<>(source, pn, EdgeType.HAS),
					new Edge<>(pn, target, EdgeType.HAS) });
		} else if (target instanceof ExpressionNode)
			return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS), new Edge<>(target,
					createVariableNode(((ExpressionNode) target).getVariableName()), EdgeType.HAS) });

		else
			return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
	}
}
