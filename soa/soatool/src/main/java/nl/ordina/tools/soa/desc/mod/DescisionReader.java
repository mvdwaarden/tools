package nl.ordina.tools.soa.desc.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.soa.desc.graph.dm.DescisionNode;
import nl.ordina.tools.soa.desc.graph.parsers.DescisionSAXHandler;

public class DescisionReader extends GraphReader<DescisionNode, Edge<DescisionNode>> {
	public DescisionReader(String baseUrl) {
		super(baseUrl);
	}

	@Override
	public Graph<DescisionNode, Edge<DescisionNode>> read(String file) {
		Graph<DescisionNode, Edge<DescisionNode>> result = null;

		result = read(file, new DescisionSAXHandler(new Graph<>()));

		return result;
	}
}
