package nl.ordina.tools.soa.bpmn.graph.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.bpmn.graph.dm.BPMNNode;

public class BPMNSAXHandler extends GraphSAXHandler<BPMNNode, Edge<BPMNNode>> {
	public BPMNSAXHandler(Graph<BPMNNode, Edge<BPMNNode>> gra) {
		super(gra);
	}
	@Override
	public GraphHandlerFactory<BPMNNode, Edge<BPMNNode>> createNodeFactory() {
		return new BPMNFactory(this);
	}
}
