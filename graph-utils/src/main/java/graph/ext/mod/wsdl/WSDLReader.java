package graph.ext.mod.wsdl;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.wsdl.WSDLNode;
import graph.ext.parser.wsdl.WSDLSAXHandler;
import graph.util.GraphReader;

public class WSDLReader extends GraphReader<WSDLNode, Edge<WSDLNode>> {
	public WSDLReader(String baseUrl) {
		super(baseUrl);
	}

	public void read(String file, Graph<WSDLNode, Edge<WSDLNode>> graph) {
		read(file, new WSDLSAXHandler(graph));
	}

	public Graph<WSDLNode, Edge<WSDLNode>> read(String file) {
		Graph<WSDLNode, Edge<WSDLNode>> graph = new Graph<>();
		read(file, new WSDLSAXHandler(graph));

		return graph;
	}
}
