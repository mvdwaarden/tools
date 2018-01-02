package nl.ordina.tools.soa.soapui.graph.parser;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUINode;

public class SOAPUIConfigurationSAXHandler extends GraphSAXHandler<SOAPUINode, Edge<SOAPUINode>> {

	public SOAPUIConfigurationSAXHandler(Graph<SOAPUINode, Edge<SOAPUINode>> graph) {
		super(graph);
	}

	@Override
	public GraphHandlerFactory<SOAPUINode, Edge<SOAPUINode>> createNodeFactory() {
		return new SOAPUIConfigurationFactory(this);
	}
}
