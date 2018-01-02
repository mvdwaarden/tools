package graph.dm;

import graph.dm.GraphIndex.Direction;

public interface GraphFilter<N extends Node, E extends Edge<N>> {
	public boolean apply(E edge, N node, Direction direction);
}
