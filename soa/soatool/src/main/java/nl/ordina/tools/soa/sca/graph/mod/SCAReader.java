package nl.ordina.tools.soa.sca.graph.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.soa.sca.graph.dm.SCANode;
import nl.ordina.tools.soa.sca.graph.parser.SCACompositeAndComponentSAXHandler;

public class SCAReader extends GraphReader<SCANode, Edge<SCANode>> {
	public SCAReader(String baseUrl) {
		super(baseUrl);
	}

	@Override
	public Graph<SCANode, Edge<SCANode>> read(String file) {
		Graph<SCANode, Edge<SCANode>> result = null;

		result = read(file, new SCACompositeAndComponentSAXHandler(new Graph<>()));		

		return result;
	}
}
