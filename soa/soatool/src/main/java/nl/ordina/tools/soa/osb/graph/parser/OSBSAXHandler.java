package nl.ordina.tools.soa.osb.graph.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.osb.graph.dm.OSBNode;

/**
 * Purpose: OSB SAX Handler for parsing OSB artefact files.
 * 
 * @author mwa17610
 * 
 */
public class OSBSAXHandler extends GraphSAXHandler<OSBNode, Edge<OSBNode>> {

	public OSBSAXHandler(Graph<OSBNode, Edge<OSBNode>> graph) {
		super(graph);
	}

	@Override
	public GraphHandlerFactory<OSBNode, Edge<OSBNode>> createNodeFactory() {
		return new OSBFactory(this);
	}
}
