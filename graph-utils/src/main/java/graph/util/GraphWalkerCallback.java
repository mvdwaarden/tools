package graph.util;

import graph.dm.Edge;
import graph.dm.Node;

/**
 * Purpose: Iterator construction that 'walks' over all the edges in a graph.
 * 
 * @author mwa17610
 * 
 */
public interface GraphWalkerCallback<N extends Node, E extends Edge<N>> {
	void walk(E edge);
}
