package graph.ext.parser.wsdl;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.wsdl.WSDLNode;
import graph.parser.GraphSAXHandler;
import graph.parser.GraphHandlerFactory;

/**
 * Purpose: XML SAX Handler for parsing WSDL files.
 * 
 * @author mwa17610
 * 
 */
public class WSDLSAXHandler extends GraphSAXHandler<WSDLNode,Edge<WSDLNode>> {
	public WSDLSAXHandler(Graph<WSDLNode,Edge<WSDLNode>> graph) {
		super(graph);
	}

	@Override
	public GraphHandlerFactory<WSDLNode,Edge<WSDLNode>> createNodeFactory() {
		return new WSDLNodeFactory(this);
	}

}
