package nl.ordina.tools.soa.bpmn.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.soa.bpmn.graph.dm.BPMNNode;
import nl.ordina.tools.soa.bpmn.graph.parser.BPMNSAXHandler;

public class BPMNReader extends GraphReader<BPMNNode, Edge<BPMNNode>> {
	public BPMNReader(String baseUrl) {
		super(baseUrl);
	}

	@Override
	public Graph<BPMNNode, Edge<BPMNNode>> read(String file) {
		Graph<BPMNNode, Edge<BPMNNode>> result = null;

		result = read(file, new BPMNSAXHandler(new Graph<>()));

		return result;
	}
}
