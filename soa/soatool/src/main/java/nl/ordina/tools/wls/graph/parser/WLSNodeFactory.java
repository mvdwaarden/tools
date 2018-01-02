package nl.ordina.tools.wls.graph.parser;

import java.util.List;

import org.xml.sax.Attributes;

import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.wls.WLSConst;
import nl.ordina.tools.wls.graph.dm.CapacityNode;
import nl.ordina.tools.wls.graph.dm.ConstraintNode;
import nl.ordina.tools.wls.graph.dm.ThreadConstraintNode;
import nl.ordina.tools.wls.graph.dm.WLSNode;
import nl.ordina.tools.wls.graph.dm.WorkManagerNode;

public class WLSNodeFactory extends GraphHandlerFactory<WLSNode, Edge<WLSNode>> {
	public WLSNodeFactory(WLSSAXHandler handler) {
		super(handler);
	}

	@Override
	public WLSNode createNode(String uri, String localName, String qName, Attributes atts) {
		WLSNode result = null;

		if (nodeTest(qName, WLSConst.NS_WLS_DOMAIN, WLSConst.EL_WORK_MANAGER, uri, localName)) {
			result = new WorkManagerNode();
		} else if (nodeTest(qName, WLSConst.NS_WLS_DOMAIN, WLSConst.EL_MAX_THREAD_CONSTRAINT, uri, localName)) {
			result = new ThreadConstraintNode(ThreadConstraintNode.Type.MAX_THREAD);
		} else if (nodeTest(qName, WLSConst.NS_WLS_DOMAIN, WLSConst.EL_CAPACITY, uri, localName)) {
			result = new CapacityNode();
		} else if (pathTest("/domain")) {
			result = new WLSNode();
			setNodeValuesIfEmpty(result, getRelativeId());
		}

		return result;
	}

	@Override
	public void postNodeCreation(WLSNode currentNode, String uri, String localName, String qName, StringBuilder data) {
		if (pathTest("/domain/name")) {
			currentNode.setDescription(data.toString());
			currentNode.setName(data.toString());
		} else if (nodeTest(qName, null, WLSConst.EL_NAME, null, localName)
				&& (currentNode.getId() == null || currentNode.getId().isEmpty())) {
			String name = data.toString();
			currentNode.setId(name);
			currentNode.setDescription(name);
			currentNode.setName(name);
		} else if (nodeTest(qName, null, WLSConst.EL_COUNT, null, localName) && currentNode instanceof ConstraintNode) {
			((ConstraintNode) currentNode).setCount(Integer.parseInt(data.toString()));
		} else
			if (nodeTest(qName, null, WLSConst.EL_TARGET, null, localName) && currentNode instanceof ConstraintNode) {
			((ConstraintNode) currentNode).setTarget(data.toString());
		} else if (pathTest("work-manager/" + WLSConst.EL_MAX_THREAD_CONSTRAINT)
				|| pathTest("work-manager/" + WLSConst.EL_CAPACITY)) {
			currentNode.setId(data.toString());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<WLSNode>> createEdges(WLSNode source, WLSNode target) {
		return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
	}
}
