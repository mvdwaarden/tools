package nl.ordina.tools.soa.sca.graph.parser;

import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import data.StringUtil;
import graph.dm.Edge;
import graph.ext.WSDLUtil;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.soa.sca.SCAConst;
import nl.ordina.tools.soa.sca.graph.dm.ComponentNode;
import nl.ordina.tools.soa.sca.graph.dm.ComponentNode.ImplementationType;
import nl.ordina.tools.soa.sca.graph.dm.CompositeNode;
import nl.ordina.tools.soa.sca.graph.dm.ReferenceNode;
import nl.ordina.tools.soa.sca.graph.dm.SCANode;
import nl.ordina.tools.soa.sca.graph.dm.ServiceNode;
import nl.ordina.tools.soa.sca.graph.dm.WireNode;
import nl.ordina.tools.soa.sca.graph.dm.Interface;

/**
 * <pre>
 * Composite beschrijft de nieuwe component, die zelf bestaat uit componenten en
 * externe referenties.
 * 
 * De composite levert services die door andere composites gebruikt kunnen
 * worden.
 * 
 * De composite beschrijft de 'bedrading' tussen de componenten, services en
 * externe referenties.
 * 
 * Naamsgevings conventie: 
 * - component : <naam> 
 * - reference : <naam> 
 * - service : <naam>
 * 
 * Bedrading: 
 * - component (reference) -> reference 
 *   <naam component>/<naam reference in component> -> <naam reference in composite> 
 * - component -> component (service) 
 *   <naam component>/<naam reference in component> -> <naam component>/<naam service in component> 
 *   : LET OP - naam reference KAN extensie .service hebben! 
 * - service -> component (service) 
 *   <naam service in composite> -> <naam component>/<naam service in component>
 * 
 * Implementatie: 
 * - Component pad : relatief pad 
 * - Component referentie : prefix met relatief pad (t.o.v. analyse bron pad) 
 * - Composite referentie : prefix met relatief pad + root node ID (= composite ID)
 * 
 * Component bevat metadata van componenten die gebruikt worden in de composite. De composite zelf is GEEN component!
 * 
 * Naamsgevingsconventie van services en referenties:
 * - Naam - Extern : 
 *     service : Services.Externals.<naam>.service
 *     reference: References.Externals.<naam>.reference
 * - Naam - Intern (b.v. subproces)
 *     service : Services.Externals.<naam>.service
 *     reference: References.Externals.<naam>.reference
 * 
 * </pre>
 * 
 * @author mwa17610
 *
 */
public class SCACompositeAndComponentFactory extends GraphHandlerFactory<SCANode, Edge<SCANode>> {
	public static final String EDGE_TYPE_IMPLEMENTED_BY = "IMPLEMENTED_BY";
	public static final String EDGE_TYPE_USES = "USES";

	public SCACompositeAndComponentFactory(SCACompositeAndComponentSAXHandler handler) {
		super(handler);
	}

	@Override
	public SCANode createNode(String uri, String localName, String qName, Attributes atts) {
		SCANode result = null;

		if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_COMPONENT, uri, localName)) {
			result = new ComponentNode();
			setNodeValuesIfEmpty(result, atts);
			/**
			 * the name is also the name of the file <component>.componentType
			 * which is relative to the composite.xml.
			 */
			result.setId(DataUtil.getInstance().getRelativename(getBaseUrl(),
					getRelativePath() + DataUtil.PATH_SEPARATOR + atts.getValue(SCAConst.ATTR_NAME)));
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_COMPOSITE, uri, localName)) {
			CompositeNode cn = new CompositeNode();
			result = cn = new CompositeNode();
			setNodeValuesIfEmpty(cn,
					this.getRelativePath() + DataUtil.PATH_SEPARATOR + atts.getValue(SCAConst.ATTR_NAME));
			cn.setDescription(atts.getValue(SCAConst.ATTR_NAME));
			cn.setRevision(atts.getValue(SCAConst.ATTR_REVISION));
			cn.setCompositeLabel(atts.getValue(SCAConst.ATTR_LABEL));
			cn.setMode(atts.getValue(SCAConst.ATTR_MODE));
			cn.setState(atts.getValue(SCAConst.ATTR_STATE));
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_IMPLEMENTATION_BPMN, uri, localName)) {
			setComponentImplementation(ComponentNode.ImplementationType.BPMN, atts);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_IMPLEMENTATION_MEDIATOR, uri, localName)) {
			setComponentImplementation(ComponentNode.ImplementationType.MEDIATOR, atts);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_IMPLEMENTATION_BPEL, uri, localName)) {
			setComponentImplementation(ComponentNode.ImplementationType.BPEL, atts);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_IMPLEMENTATION_WORKFLOW, uri, localName)) {
			setComponentImplementation(ComponentNode.ImplementationType.WORKFLOW, atts);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_IMPLEMENTATION_DECISION, uri, localName)) {
			setComponentImplementation(ComponentNode.ImplementationType.DECISION, atts);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_SERVICE, uri, localName)) {
			result = createPinNode(atts, Interface.Type.SERVICE);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_REFERENCE, uri, localName)) {
			result = createPinNode(atts, Interface.Type.REFERENCE);
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_WIRE, uri, localName)) {
			result = new WireNode();
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_COMPONENT_TYPE, uri, localName)) {
			result = new ComponentNode();
			setNodeValuesIfEmpty(result, getRelativePath() + DataUtil.PATH_SEPARATOR
					+ DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()));
			result.setDescription(DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()));
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_INTERFACE_WSDL, uri, localName)
				&& getCurrentNode() instanceof Interface) {
			((Interface) getCurrentNode()).setWsdlInterface(atts.getValue(SCAConst.ATTR_INTERFACE));
			((Interface) getCurrentNode()).setWsdlCallbackInterface(atts.getValue(SCAConst.ATTR_CALLBACK_INTERFACE));
		} else if (nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_BINDING_JCA, uri, localName)
				&& getCurrentNode() instanceof Interface) {
			((Interface) getCurrentNode()).setBindingJCA(atts.getValue(SCAConst.ATTR_CONFIG));
		}

		return result;
	}

	private void setComponentImplementation(ImplementationType type, Attributes atts) {
		if (getCurrentNode() instanceof ComponentNode) {
			ComponentNode cn = (ComponentNode) getCurrentNode();
			cn.setImplementationType(type);
			cn.setSource(getBaseUrl() + DataUtil.PATH_SEPARATOR + getRelativePath() + DataUtil.PATH_SEPARATOR
					+ atts.getValue(SCAConst.ATTR_SRC));
		}
	}

	public Interface createPinNode(Attributes atts, Interface.Type type) {
		Interface result;
		String prefix = DataUtil.getInstance().getFilenameWithoutExtension(getSourceUrl()) + DataUtil.PATH_SEPARATOR;

		if (type == Interface.Type.SERVICE)
			result = new ServiceNode();
		else
			result = new ReferenceNode();
		result.setType(type);
		if (getRootNode() instanceof CompositeNode)
			setNodeValuesIfEmpty(result,
					getRootNode().getId() + DataUtil.PATH_SEPARATOR + atts.getValue(SCAConst.ATTR_NAME));
		else
			setNodeValuesIfEmpty(result,
					getRelativePath() + DataUtil.PATH_SEPARATOR + prefix + atts.getValue(SCAConst.ATTR_NAME));
		result.setDescription(StringUtil.getInstance().replace(atts.getValue(SCAConst.ATTR_NAME),
				SCAConst.REFERENCE_NAME_REPLACE_FORMAT));
		result.setWsdlLocation(
				WSDLUtil.getInstance().createWSDLName(getBaseUrl() + DataUtil.PATH_SEPARATOR + getRelativePath(),
						atts.getValue(SCAConst.NS_SOA_DESIGNER, SCAConst.ATTR_WSDL_LOCATION)));

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<SCANode>> createEdges(SCANode source, SCANode target) {
		List<Edge<SCANode>> result;

		/*
		 * <pre> reverse the direction if the targetnode is a reference of type
		 * service.
		 * 
		 * e.g. in the XML the interface is always a child of the
		 * Component/Composite which would mean that a service would also be
		 * used by the Component/Composite, while in fact the Component,
		 * Composite implements the service.
		 * 
		 * </pre>
		 */
		if ((source instanceof ComponentNode || source instanceof CompositeNode) && target instanceof Interface
				&& ((Interface) target).getType() == Interface.Type.SERVICE) {
			result = createList(new Edge[] { new Edge<>(target, source, EDGE_TYPE_IMPLEMENTED_BY) });
		} else {
			result = createList(new Edge[] { new Edge<>(source, target, EDGE_TYPE_USES) });
		}

		return result;
	}

	@Override
	public void postNodeCreation(SCANode currentNode, String uri, String localName, String qName, StringBuilder data) {
		if (currentNode instanceof WireNode
				&& nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_SOURCE_URI, uri, localName)) {
			((WireNode) currentNode).setSourceUri(getWireUri(data.toString()));
			setWireNodeName((WireNode) currentNode);
		} else if (currentNode instanceof WireNode
				&& nodeTest(qName, SCAConst.NS_SCA, SCAConst.EL_TARGET_URI, uri, localName)) {
			((WireNode) currentNode).setTargetUri(getWireUri(data.toString()));
			setWireNodeName((WireNode) currentNode);
		}
	}

	private void setWireNodeName(WireNode wireNode) {
		if (null != wireNode.getSourceUri() && null != wireNode.getTargetUri())
			setNodeValuesIfEmpty(wireNode, wireNode.getSourceUri() + "->" + wireNode.getTargetUri());
	}

	private String getWireUri(String uri) {
		String result = uri;
		/* check if component URI */
		if (!uri.contains(DataUtil.PATH_SEPARATOR)) {
			// No its a reference, CompositeNode::getId()
			result = getRootNode().getId() + DataUtil.PATH_SEPARATOR + uri;
		} else {
			result = getRelativePath() + DataUtil.PATH_SEPARATOR + result;
		}

		if (result.equals(
				"ApplicationBeoordelen/source/Beoordelen/ApplicationBeoordelen/source/Beoordelen/Beoordelen/ZAK_SluitZaak")) {
			getWireUri(uri);
		}
		return result;
	}
}
