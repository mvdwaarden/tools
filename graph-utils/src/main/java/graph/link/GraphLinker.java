package graph.link;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import graph.GraphOption;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.GraphIndex;
import graph.dm.Node;

/**
 * Purpose: Defines if two nodes from one or more graphs can be linked.
 * 
 * @author mwa17610
 * 
 */
public class GraphLinker<N extends Node, E extends Edge<N>> {
	private List<GraphLink<N, E>> links;

	public GraphLinker(GraphLink<N, E>[] links) {
		this.links = new ArrayList<>();
		for (GraphLink<N, E> link : links)
			this.links.add(link);
	}

	/**
	 * Link the nodes
	 * 
	 * @param gra
	 */
	public void link(Graph<N, E> gra, GraphIndex<N, E> idx, Comparator<N> comparator) {
		List<E> edges = new ArrayList<>();
		for (N sourceNode : gra.getNodes()) {
			for (N targetNode : gra.getNodes()) {
				if (sourceNode != targetNode) {
					for (GraphLink<N, E> link : this.links) {
						if (link.linkit(idx, sourceNode, targetNode)) {
							List<E> testEdges = link.createEdges(sourceNode, targetNode);
							if (null != testEdges) {
								for (E edge : testEdges) {
									edges.add(edge);
								}
							}
						}
					}
				}
			}
		}
		for (E edge : edges) {
			gra.addEdge(edge, comparator, GraphOption.CHECK_DUPLICATES);
		}
	}
}
