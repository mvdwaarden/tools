package graph.ext.parser.xsd;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.xsd.XSDNode;
import graph.parser.GraphSAXHandler;

/**
 * Purpose: XML SAX Handler for parsing XSD files.
 * 
 * @author mwa17610
 * 
 */
public class XSDSAXHandler extends GraphSAXHandler<XSDNode, Edge<XSDNode>> {
	public XSDSAXHandler(Graph<XSDNode, Edge<XSDNode>> graph) {
		super(graph);
	}

	@Override
	public XSDNodeFactory createNodeFactory() {
		return new XSDNodeFactory(this);
	}
}
