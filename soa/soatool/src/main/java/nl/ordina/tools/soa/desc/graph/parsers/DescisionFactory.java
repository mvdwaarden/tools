package nl.ordina.tools.soa.desc.graph.parsers;

import java.util.List;

import org.xml.sax.Attributes;

import graph.dm.Edge;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.soa.bpmn.BPMNConst;
import nl.ordina.tools.soa.bpmn.graph.dm.TaskNode;
import nl.ordina.tools.soa.desc.DescisionConst;
import nl.ordina.tools.soa.desc.graph.dm.DescisionNode;
import nl.ordina.tools.soa.desc.graph.dm.RuleRepositoryNode;

/**
 * 
 * @author mwa17610
 *
 */
public class DescisionFactory extends GraphHandlerFactory<DescisionNode, Edge<DescisionNode>> {
	public DescisionFactory(DescisionSAXHandler handler) {
		super(handler);
	}

	@Override
	public DescisionNode createNode(String uri, String localName, String qName, Attributes atts) {
		DescisionNode result = null;
		if (nodeTest(qName, DescisionConst.NS_RULE_DICTIONARY, DescisionConst.EL_RULE_DICTIONARY, uri, localName)) {
			result = new RuleRepositoryNode();
			result.setId(atts.getValue(DescisionConst.ATTR_ID));
		}
		return result;
	}

	@Override
	public void postNodeCreation(DescisionNode currentNode, String uri, String localName, String qName,
			StringBuilder data) {
		if (nodeTest(qName, null, DescisionConst.EL_NAME, null, localName)
				&& currentNode instanceof RuleRepositoryNode) {
			currentNode.setName(data.toString());
			currentNode.setDescription(data.toString());
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public List<Edge<DescisionNode>> createEdges(DescisionNode source, DescisionNode target) {

		List<Edge<DescisionNode>> result = null;

		result = createList(new Edge[] { new Edge<>(source, target) });

		return result;
	}
}
