package graph.io;

import graph.dm.Cluster;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;

public interface CustomDottyMarkupCallback<N extends Node, E extends Edge<N>> {
	String nodeMarkup(Graph<N, E> graph, N node, int referenceCount);

	String[] clusterMarkup(Graph<N, E> graph, Cluster<N> cluster, int depth);

	String edgeMarkup(Graph<N, E> gra, E edge);
}
