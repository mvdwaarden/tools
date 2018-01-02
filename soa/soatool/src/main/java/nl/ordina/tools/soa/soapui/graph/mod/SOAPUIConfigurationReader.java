package nl.ordina.tools.soa.soapui.graph.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.soa.soapui.graph.dm.SOAPUINode;
import nl.ordina.tools.soa.soapui.graph.parser.SOAPUIConfigurationSAXHandler;

public class SOAPUIConfigurationReader extends GraphReader<SOAPUINode,Edge<SOAPUINode>> {

	@Override
	public Graph<SOAPUINode, Edge<SOAPUINode>> read(String file) {
		Graph<SOAPUINode,Edge<SOAPUINode>> result = new Graph<>();
		
		super.read(file, new SOAPUIConfigurationSAXHandler(result));
		
		return result;
	}
}
