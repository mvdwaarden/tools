package graph.dm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphIndex<N extends Node, E extends Edge<N>> {
	public enum Direction {
		FOLLOW, OPPOSITE
	}

	private NodeEdgeCache cacheSource2Edge;
	private NodeEdgeCache cacheTarget2Edge;
	private Graph<N, E> graph;

	public GraphIndex(Graph<N, E> gra) {
		this.graph = gra;
	}

	public GraphIndex<N, E> build() {
		cacheSource2Edge = new NodeEdgeCache();
		cacheTarget2Edge = new NodeEdgeCache();

		for (E edge : graph.getEdges()) {
			cacheSource2Edge.add(edge.getSource(), edge);
			cacheTarget2Edge.add(edge.getTarget(), edge);
		}

		return this;
	}

	public List<E> getEdges(N node, Direction dir) {
		List<E> result = null;
		switch (dir) {
		case OPPOSITE:
			result = cacheTarget2Edge.get(node);
			break;
		case FOLLOW:
			result = cacheSource2Edge.get(node);
			break;
		}

		return result;
	}

	private class NodeEdgeCache {
		private Map<N, List<E>> cache;

		public NodeEdgeCache() {
			this.cache = new HashMap<>();
		}

		public final void add(N node, E edge) {
			List<E> edges = cache.get(node);

			if (null == edges) {
				edges = new ArrayList<>();
				cache.put(node, edges);
			}
			edges.add(edge);
		}

		public final List<E> get(N node) {
			return cache.get(node);
		}
	}
}
