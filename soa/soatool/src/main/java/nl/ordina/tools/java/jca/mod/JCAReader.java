package nl.ordina.tools.java.jca.mod;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.util.GraphReader;
import nl.ordina.tools.java.jca.JCAConst;
import nl.ordina.tools.java.jca.dm.JCANode;
import nl.ordina.tools.java.jca.parser.JCASAXHandler;

public class JCAReader extends GraphReader<JCANode, Edge<JCANode>> {
	public JCAReader(String baseUrl) {
		super(baseUrl);
	}

	@Override
	public Graph<JCANode, Edge<JCANode>> read(String file) {
		Graph<JCANode, Edge<JCANode>> result = null;

		if (file.toLowerCase().endsWith(JCAConst.FILE_EXTENSION_JCA.toLowerCase()))
			result = super.read(file, new JCASAXHandler(new Graph<>()));

		return result;
	}
}
