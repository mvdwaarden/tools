package nl.ordina.tools.wls.graph.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.wls.graph.dm.WLSNode;
import nl.ordina.tools.wls.graph.parser.WLSSAXHandler;

public class WLSReader extends GraphReader<WLSNode, Edge<WLSNode>> {	
	public WLSReader(String baseUrl) {
		super(baseUrl);				
	}

	public void read(String file, Graph<WLSNode, Edge<WLSNode>> graph) {
		read(file, new WLSSAXHandler(graph));
	}

	public Graph<WLSNode, Edge<WLSNode>> read(String file) {
		Graph<WLSNode, Edge<WLSNode>> graph = new Graph<>();
		read(file, new WLSSAXHandler(graph));

		return graph;
	}
}
