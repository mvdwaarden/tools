package graph.ext.parser.xsd;

import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import data.StringUtil;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.ext.XSDConst;
import graph.ext.dm.xsd.XSDComplexTypeNode;
import graph.ext.dm.xsd.XSDElementNode;
import graph.ext.dm.xsd.XSDNode;
import graph.ext.dm.xsd.XSDSimpleTypeNode;
import graph.parser.GraphHandlerFactory;

/**
 * Purpose: Factory which creates XSD nodes during XSD SAX parsing.
 * 
 * @author mwa17610
 * 
 */
public class XSDNodeFactory extends GraphHandlerFactory<XSDNode, Edge<XSDNode>> {
	public XSDNodeFactory(XSDSAXHandler handler) {
		super(handler);
	}

	private void setXSDNodeValues(XSDNode node, String name) {
		String namespace = getRootNode().getNamespace();
		
		if (null == name || name.isEmpty()) {
			List<XSDNode> path = filterNodesToRoot(n -> n instanceof XSDElementNode);
			StringBuilder sbName = new StringBuilder();
			for (XSDNode n : path) {
				sbName.append(n.getDescription());
				sbName.append(".");
			}
			StringUtil.getInstance().stripEnd(sbName, ".");
			node.setId(namespace + DataUtil.PATH_SEPARATOR + sbName.toString());
			node.setName(sbName.toString());
			node.setDescription(sbName.toString());
		} else if (!name.startsWith(namespace)) {
			node.setId(namespace + DataUtil.PATH_SEPARATOR + name);
			node.setName(name);
			node.setDescription(name);
		} else {
			node.setId(name);
			node.setName(name);
			node.setDescription(name);
		}
	}

	public XSDNode createXSDNode(Attributes atts) {
		XSDNode result = new XSDNode();

		String value = atts.getValue(XSDConst.ATTR_NAMESPACE);
		if (null != value) {
			result.setNamespace(value);
		} else {
			value = atts.getValue(XSDConst.ATTR_TARGET_NAMESPACE);
			if (null != value)
				result.setNamespace(value);
		}
		// set schema location => resolve according to relative path!
		value = atts.getValue(XSDConst.ATTR_SCHEMALOCATION);
		if (null != value) {
			String relativeId = value;
			if (!DataUtil.getInstance().isAbsolutePath(value))
				relativeId = getRelativePath() + DataUtil.PATH_SEPARATOR + value;
			result.setSchemaLocation(DataUtil.getInstance().simplifyFolder(relativeId));
			result.setDescription(DataUtil.getInstance().getFilenameWithoutExtension(relativeId));
			result.setName(result.getDescription());
		}
		result.setId(result.getNamespace());

		return result;
	}

	@Override
	public XSDNode createNode(String uri, String localName, String qName, Attributes atts) {
		XSDNode result = null;

		if (nodeTest(qName, null, XSDConst.EL_IMPORT, null, localName)) {
			// schema location is optional
			result = createXSDNode(atts);
		} else if (nodeTest(qName, null, XSDConst.EL_SCHEMA, null, localName)) {
			result = createXSDNode(atts);
		} else if (nodeTest(qName, null, XSDConst.EL_EXTENSION, null, localName)
				&& getCurrentNode() instanceof XSDComplexTypeNode) {
			XSDComplexTypeNode ctn = (XSDComplexTypeNode) getCurrentNode();
			String base = atts.getValue(XSDConst.ATTR_BASE);
			ctn.setBase(expandPrefix(base));
			ctn.setExtension(true);
		} else if (nodeTest(qName, null, XSDConst.EL_RESTRICTION, null, localName)
				&& getCurrentNode() instanceof XSDComplexTypeNode) {
			XSDComplexTypeNode ctn = (XSDComplexTypeNode) getCurrentNode();
			String base = atts.getValue(XSDConst.ATTR_BASE);
			ctn.setBase(expandPrefix(base));
		} else if (nodeTest(qName, null, XSDConst.EL_COMPLEX_TYPE, null, localName)) {
			XSDComplexTypeNode ctn;
			result = ctn = new XSDComplexTypeNode();
			String name = atts.getValue(XSDConst.ATTR_NAME);
			setXSDNodeValues(result, name);
			ctn.setAbstractType(Boolean.parseBoolean(atts.getValue(XSDConst.ATTR_ABSTRACT)));
		} else if (nodeTest(qName, null, XSDConst.EL_SIMPLE_TYPE, null, localName)) {
			result = new XSDSimpleTypeNode();
			String name = atts.getValue(XSDConst.ATTR_NAME);
			setXSDNodeValues(result, name);
		} else if (nodeTest(qName, null, XSDConst.EL_ELEMENT, null, localName)) {
			XSDElementNode el;
			result = el = new XSDElementNode();
			el.setType(expandPrefix(atts.getValue(XSDConst.ATTR_TYPE)));
			el.setRef(expandPrefix(atts.getValue(XSDConst.ATTR_REF)));
			result.setId(StringUtil.getInstance().getFirstNotNullAndNotEmpyString(
					getCurrentNode().getId() + DataUtil.PATH_SEPARATOR + atts.getValue(XSDConst.ATTR_NAME),
					el.getRef()));
			result.setDescription(DataUtil.getInstance().getFilename(result.getId()));
			result.setName(result.getDescription());
		}

		if (null != result) {
			setNodeValuesIfEmpty(result, atts);

			if (null != result.getId() && null == result.getName())
				result.setName(result.getId());
		}

		return result;
	}

	@Override
	public void postNodeCreation(XSDNode currentNode, String uri, String localName, String qName, StringBuilder data) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<XSDNode>> createEdges(XSDNode source, XSDNode target) {
		return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
	}

}
