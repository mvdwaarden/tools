package nl.ordina.tools.soa.osb.graph.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.soa.osb.graph.dm.OSBNode;
import nl.ordina.tools.soa.osb.graph.parser.OSBSAXHandler;

public class OSBReader extends GraphReader<OSBNode, Edge<OSBNode>> {	
	public OSBReader(String baseUrl) {
		super(baseUrl);				
	}

	public void read(String file, Graph<OSBNode, Edge<OSBNode>> graph) {
		read(file, new OSBSAXHandler(graph));
	}

	public Graph<OSBNode, Edge<OSBNode>> read(String file) {
		Graph<OSBNode, Edge<OSBNode>> graph = new Graph<>();
		read(file, new OSBSAXHandler(graph));

		return graph;
	}
}
