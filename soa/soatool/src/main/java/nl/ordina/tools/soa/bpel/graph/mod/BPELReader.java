package nl.ordina.tools.soa.bpel.graph.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.soa.bpel.graph.dm.BPELNode;
import nl.ordina.tools.soa.bpel.graph.parser.BPELSAXHandler;


public class BPELReader extends GraphReader<BPELNode, Edge<BPELNode>> {	
	public BPELReader(String baseUrl) {
		super(baseUrl);				
	}

	public void read(String file, Graph<BPELNode, Edge<BPELNode>> graph) {
		read(file, new BPELSAXHandler(graph));
	}

	public Graph<BPELNode, Edge<BPELNode>> read(String file) {
		Graph<BPELNode, Edge<BPELNode>> graph = new Graph<>();
		read(file, new BPELSAXHandler(graph));

		return graph;
	}
}
