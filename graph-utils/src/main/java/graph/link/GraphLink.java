package graph.link;

import graph.dm.Edge;
import graph.dm.GraphIndex;
import graph.dm.Node;
import graph.parser.EdgeFactory;

/**
 * Purpose: Definition of one end of the link between nodes of one or more
 * graphs.
 * 
 * @author mwa17610
 * 
 */
public abstract class GraphLink<N extends Node, E extends Edge<N>> implements EdgeFactory<N, E> {
	public abstract boolean linkit(GraphIndex<N, E> idx, N source, N target);
}
