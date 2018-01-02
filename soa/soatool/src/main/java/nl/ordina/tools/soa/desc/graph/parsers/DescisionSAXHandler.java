package nl.ordina.tools.soa.desc.graph.parsers;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.desc.graph.dm.DescisionNode;

public class DescisionSAXHandler extends GraphSAXHandler<DescisionNode, Edge<DescisionNode>> {
	public DescisionSAXHandler(Graph<DescisionNode, Edge<DescisionNode>> gra) {
		super(gra);
	}
	@Override
	public GraphHandlerFactory<DescisionNode, Edge<DescisionNode>> createNodeFactory() {
		return new DescisionFactory(this);
	}
}
