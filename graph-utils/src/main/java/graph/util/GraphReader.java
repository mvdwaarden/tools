package graph.util;

import data.LogUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import graph.parser.GraphContentHandler;

/**
 * Purpose: Read graph information from a file.
 * 
 * @author mwa17610
 * 
 */
public abstract class GraphReader<N extends Node, E extends Edge<N>> {
	private String baseUrl = "";

	public GraphReader(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public GraphReader() {
	}

	abstract public Graph<N, E> read(String file);

	public Graph<N, E> read(String file, GraphContentHandler<N, E> handler) {
		Graph<N, E> result = new Graph<>();
		
		try {
			result = GraphUtil.getInstance().read(baseUrl, file, handler);
		} catch (Exception e) {
			LogUtil.getInstance().error("problem reading graph from [" + file + "]", e);
		}
		return result;
	}
}
