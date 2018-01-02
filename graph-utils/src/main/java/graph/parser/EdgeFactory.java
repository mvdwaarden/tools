package graph.parser;

import java.util.List;

import graph.dm.Edge;
import graph.dm.Node;

public interface EdgeFactory<N extends Node, E extends Edge<N>> {
	/**
	 * Create edges based on source and target node.
	 * 
	 * In theory this allows for node splitting.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public List<E> createEdges(N source, N target);
}
