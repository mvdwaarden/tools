package graph.ext.mod.xsd;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.xsd.XSDNode;
import graph.ext.parser.xsd.XSDSAXHandler;
import graph.util.GraphReader;

public class XSDReader extends GraphReader<XSDNode, Edge<XSDNode>> {
	public XSDReader(String baseUrl) {
		super(baseUrl);
	}

	public Graph<XSDNode, Edge<XSDNode>> read(String file) {
		Graph<XSDNode, Edge<XSDNode>> result = new Graph<>();

		read(file, result);

		return result;
	}

	public void read(String file, Graph<XSDNode, Edge<XSDNode>> graph) {
		read(file, new XSDSAXHandler(graph));
	}
}
