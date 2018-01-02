package nl.ordina.tools.soa.sca.graph.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.sca.graph.dm.SCANode;

public class SCACompositeAndComponentSAXHandler extends GraphSAXHandler<SCANode, Edge<SCANode>> {

	public SCACompositeAndComponentSAXHandler(Graph<SCANode, Edge<SCANode>> graph) {
		super(graph);
	}

	@Override
	public GraphHandlerFactory<SCANode, Edge<SCANode>> createNodeFactory() {
		return new SCACompositeAndComponentFactory(this);
	}

}
