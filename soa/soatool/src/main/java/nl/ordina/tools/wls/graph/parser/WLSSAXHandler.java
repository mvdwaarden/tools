package nl.ordina.tools.wls.graph.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphSAXHandler;
import graph.parser.GraphHandlerFactory;
import nl.ordina.tools.wls.graph.dm.WLSNode;

/**
 * Purpose: WLS SAX Handler for parsing OSB artefact files.
 * 
 * @author mwa17610
 * 
 */
public class WLSSAXHandler extends GraphSAXHandler<WLSNode, Edge<WLSNode>> {
	public WLSSAXHandler(Graph<WLSNode, Edge<WLSNode>> graph) {
		super(graph);
	}

	@Override
	public GraphHandlerFactory<WLSNode, Edge<WLSNode>> createNodeFactory() {
		return new WLSNodeFactory(this);
	}
}
