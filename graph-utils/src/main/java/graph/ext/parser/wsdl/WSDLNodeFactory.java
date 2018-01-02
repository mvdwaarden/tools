package graph.ext.parser.wsdl;

import java.util.List;

import org.xml.sax.Attributes;

import data.ConfigurationUtil;
import data.DataUtil;
import data.StringUtil;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.ext.WSDLConst;
import graph.ext.dm.wsdl.BindingNode;
import graph.ext.dm.wsdl.ImportNode;
import graph.ext.dm.wsdl.InterfaceNode;
import graph.ext.dm.wsdl.JCAOperationNode;
import graph.ext.dm.wsdl.OperationNode;
import graph.ext.dm.wsdl.PartnerLinkTypeNode;
import graph.ext.dm.wsdl.PortNode;
import graph.ext.dm.wsdl.RoleNode;
import graph.ext.dm.wsdl.WebServiceNode;
import graph.ext.dm.wsdl.WSDLNode;
import graph.parser.GraphHandlerFactory;

/**
 * Purpose: Factory which creates WSDL nodes during WSDL SAX parsing.
 * 
 * @author mwa17610
 * 
 */
public class WSDLNodeFactory extends GraphHandlerFactory<WSDLNode, Edge<WSDLNode>> {
	public WSDLNodeFactory(WSDLSAXHandler handler) {
		super(handler);
	}

	@Override
	public WSDLNode createNode(String uri, String localName, String qName, Attributes atts) {
		WSDLNode result = null;
		if (nodeTest(qName, WSDLConst.NS_XML_SCHEMA, WSDLConst.EL_IMPORT, uri, localName)) {
			result = createImportNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_ORACLE_BPEL_WSDL_JCA, WSDLConst.EL_OPERATION, uri, localName)) {
			result = createJCAOperationNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_WSDL, WSDLConst.EL_OPERATION, uri, localName)) {
			result = createOperationNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_PARTNER_LINK, WSDLConst.EL_PORT_TYPE, uri, localName)) {
			result = createInterfaceNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_WSDL, WSDLConst.EL_PORT_TYPE, uri, localName)) {
			result = createInterfaceNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_PARTNER_LINK, WSDLConst.EL_PARTNER_LINK_TYPE, uri, localName)) {
			result = createPartnerLinkTypeNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_PARTNER_LINK, WSDLConst.EL_ROLE, uri, localName)) {
			result = createRoleNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_WSDL, WSDLConst.EL_SERVICE, uri, localName)) {
			result = createServiceNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_WSDL, WSDLConst.EL_BINDING, uri, localName)) {
			result = createBindingNode(atts);
		} else if (nodeTest(qName, WSDLConst.NS_SOAP, WSDLConst.EL_BINDING, uri, localName)
				&& getCurrentNode() instanceof BindingNode) {
			((BindingNode) getCurrentNode()).setTransport(atts.getValue(WSDLConst.ATTR_TRANSPORT));
		} else if (nodeTest(qName, WSDLConst.NS_SOAP_WSDL, WSDLConst.EL_PORT, uri, localName)) {
			result = createPortNode(atts);
		} else if (localName.equals(WSDLConst.EL_DEFINITIONS)) {
			result = createWSDLNode(uri, localName, qName, atts);
		}
		if (null != result && result.getClass() != WSDLNode.class) {
			setNodeValuesIfEmpty(result, atts);
			if (null != result.getId() && null == result.getDescription())
				result.setDescription(result.getId());
			if (null != result.getId() && null == result.getName())
				result.setName(result.getId());
		}

		return result;
	}

	public BindingNode createBindingNode(Attributes atts) {
		BindingNode result = new BindingNode();

		setQualifiedName(result, atts);

		String value = atts.getValue(WSDLConst.ATTR_TYPE);

		if (null != value)
			result.setTypeValue(expandPrefix(value));

		return result;
	}

	public PortNode createPortNode(Attributes atts) {
		PortNode result = new PortNode();

		setQualifiedName(result, atts);

		String value = atts.getValue(WSDLConst.ATTR_BINDING);

		if (null != value)
			result.setBindingValue(expandPrefix(value));

		return result;
	}

	public WebServiceNode createServiceNode(Attributes atts) {
		WebServiceNode result = new WebServiceNode();

		setQualifiedName(result, atts);

		return result;
	}

	public ImportNode createImportNode(Attributes atts) {
		ImportNode result = new ImportNode();

		result.setSchemaLocation(atts.getValue(WSDLConst.ATTR_SCHEMALOCATION));
		result.setNamespace(atts.getValue(WSDLConst.ATTR_NAMESPACE));
		result.setId(result.getNamespace());
		result.setDescription(result.getNamespace());

		return result;
	}

	public RoleNode createRoleNode(Attributes atts) {
		RoleNode result = new RoleNode();

		setQualifiedName(result, atts);

		return result;
	}

	public InterfaceNode createInterfaceNode(Attributes atts) {
		InterfaceNode result = new InterfaceNode();

		setQualifiedName(result, atts);

		return result;
	}

	public PartnerLinkTypeNode createPartnerLinkTypeNode(Attributes atts) {
		PartnerLinkTypeNode result = new PartnerLinkTypeNode();

		setQualifiedName(result, atts);

		return result;
	}

	public OperationNode createOperationNode(Attributes atts) {
		OperationNode result = new OperationNode();

		setQualifiedName(result, atts);

		return result;
	}

	public JCAOperationNode createJCAOperationNode(Attributes atts) {
		JCAOperationNode result = new JCAOperationNode();

		result.setDestinationName(atts.getValue(WSDLConst.ATTR_DESTINATION_NAME));
		result.setPayloadType(atts.getValue(WSDLConst.ATTR_PAYLOAD_TYPE));

		return result;
	}

	public WSDLNode createWSDLNode(String uri, String localName, String qName, Attributes atts) {
		WSDLNode result = new WSDLNode();
		String targetNamespace = atts.getValue(WSDLConst.ATTR_TARGET_NAMESPACE);
		String name = atts.getValue(WSDLConst.ATTR_NAME);
		if (null == name || name.isEmpty())
			name = DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl());

		name = replaceBySetting(name, result, "name");

		if (null != name && null != targetNamespace && !name.isEmpty() && !targetNamespace.isEmpty())
			setNodeValuesIfEmpty(result, targetNamespace + DataUtil.PATH_SEPARATOR + name);
		else
			setNodeValuesIfEmpty(result, getRelativeId());
		result.setDescription(DataUtil.getInstance().getFilenameWithoutExtension(result.getId()));

		return result;
	}

	@Override
	public void postNodeCreation(WSDLNode currentNode, String uri, String localName, String qName, StringBuilder data) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<WSDLNode>> createEdges(WSDLNode source, WSDLNode target) {
		return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
	}

	public String replaceBySetting(String str, Object obj, String value) {
		String result = str;

		String replaceConfig = ConfigurationUtil.getInstance()
				.getSetting(obj.getClass().getName() + "." + value + ".replace");

		result = StringUtil.getInstance().replace(str, replaceConfig);

		return result;
	}
}
