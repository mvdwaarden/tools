package nl.ordina.tools.soa.bpel.graph.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.bpel.graph.dm.BPELNode;

public class BPELSAXHandler extends GraphSAXHandler<BPELNode, Edge<BPELNode>> {

	public BPELSAXHandler(Graph<BPELNode, Edge<BPELNode>> graph) {
		super(graph);
	}

	@Override
	public GraphHandlerFactory<BPELNode, Edge<BPELNode>> createNodeFactory() {
		return new BPELFactory(this);
	}
}
