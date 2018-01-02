package nl.ordina.tools.soa.bpel.graph.parser;

import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.bpel.BPELConst;
import nl.ordina.tools.soa.bpel.graph.dm.BPELNode;
import nl.ordina.tools.soa.bpel.graph.dm.InvokeNode;
import nl.ordina.tools.soa.bpel.graph.dm.ScopeNode;

public class BPELFactory extends GraphHandlerFactory<BPELNode, Edge<BPELNode>> {

	public BPELFactory(GraphSAXHandler<BPELNode, Edge<BPELNode>> handler) {
		super(handler);
	}

	@Override
	public BPELNode createNode(String uri, String localName, String qName, Attributes atts) {
		BPELNode result = null;
		if (nodeTest(qName, null, localName, null, BPELConst.EL_PROCESS)) {
			result = new BPELNode();
			setNodeValuesIfEmpty(result, getRelativePath() + DataUtil.PATH_SEPARATOR + atts.getValue(BPELConst.ATTR_NAME));
			result.setDescription(atts.getValue(BPELConst.ATTR_NAME));
		} else if (nodeTest(qName, null, localName, null, BPELConst.EL_SCOPE)) {
			result = new ScopeNode();
			setNodeValuesIfEmpty(result, atts, BPELConst.ATTR_NAME, true);
		} else if (nodeTest(qName, null, localName, null, BPELConst.EL_INVOKE)) {
			BPELNode currentNode = getCurrentNode();
			if (currentNode instanceof ScopeNode) {
				InvokeNode in;
				result = in = new InvokeNode();
				in.setOperation(atts.getValue(BPELConst.ATTR_OPERATION));
				in.setPartnerLink(atts.getValue(BPELConst.ATTR_PARTNERLINK));
				setNodeValuesIfEmpty(result, currentNode.getName() + DataUtil.PATH_SEPARATOR + in.getPartnerLink()
						+ DataUtil.PATH_SEPARATOR + in.getOperation());
				result.setDescription(in.getPartnerLink() + DataUtil.PATH_SEPARATOR + in.getOperation());
			}
		} else if (nodeTest(qName, uri, localName, BPELConst.NS_RESOURCES_CONFIG, BPELConst.EL_SERVICE)) {
			BPELNode currentNode = getCurrentNode();
			if (currentNode instanceof InvokeNode) {
				InvokeNode in = (InvokeNode) currentNode;
				in.setReference(atts.getValue(BPELConst.ATTR_REFERENCE));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<BPELNode>> createEdges(BPELNode source, BPELNode target) {
		return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
	}

	@Override
	public void postNodeCreation(BPELNode currentNode, String uri, String localName, String qName, StringBuilder data) {

	}
}
