package nl.ordina.tools.java.jca.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.java.jca.dm.JCANode;

public class JCASAXHandler extends GraphSAXHandler<JCANode, Edge<JCANode>> {
	public JCASAXHandler(Graph<JCANode, Edge<JCANode>> gra) {
		super(gra);
	}
	@Override
	public GraphHandlerFactory<JCANode, Edge<JCANode>> createNodeFactory() {
		return new JCAFactory(this);
	}
}
