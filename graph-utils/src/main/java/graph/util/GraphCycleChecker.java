package graph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;

/**
 * Check a graph for cycles
 * 
 * @author mwa17610
 * 
 */
public class GraphCycleChecker<N extends Node, E extends Edge<N>> {
	public List<List<E>> checkCycles(Graph<N, E> graph, boolean all) {
		return checkCycles(graph, null, all);
	}

	public List<List<E>> checkCycles(Graph<N, E> graph, String[] edgeNames, boolean all) {
		List<List<E>> result = new ArrayList<>();
		CheckContext ctx = new CheckContext();
		ctx._cycles = result;
		ctx._graph = graph;
		ctx._all = all;
		ctx._edges = graph.getEdges();
		// Start from each node and check for cycles
		for (N node : graph.getNodes()) {
			// Create a new breadcrum
			ctx._breadcrum = new ArrayList<>();
			ctx._edges = new ArrayList<>();
			for (E edge : graph.getEdges())
				ctx._edges.add(edge);
			// Start checking
			checkCycle(node, ctx, edgeNames);
		}

		return result;
	}

	/**
	 * Cleanup cycles
	 */
	public void cleanup(List<List<E>> cycles) {
		// Cleanup bread crums
		for (List<E> cycle : cycles)
			cleanupBreadcrum(cycle);
	}

	/**
	 * Checks if the node is contained in one of the cycles
	 */

	public boolean isNodeContainedInCycles(List<List<E>> cycles, N node) {
		Optional<List<E>> result = cycles.stream().filter(cycle -> {
			Optional<E> optEdge = cycle.stream()
					.filter(e -> e.getSource().treatAsSame(node) || e.getTarget().treatAsSame(node)).findFirst();
			return optEdge.isPresent();
		}).findFirst();

		return result.isPresent();
	}

	/**
	 * Checks if the node is contained in one of the cycles
	 */

	public boolean isEdgeContainedInCycles(List<List<E>> cycles, E edge) {
		Optional<List<E>> result = cycles.stream().filter(cycle -> {
			Optional<E> optEdge = cycle.stream().filter(e -> edge.equals(e)).findFirst();
			return optEdge.isPresent();
		}).findFirst();

		return result.isPresent();
	}

	/**
	 * Clean up a breadcrum
	 * 
	 * @param edges
	 */
	private void cleanupBreadcrum(List<E> edges) {
		List<N> nodes = new ArrayList<>(edges.size());

		for (E edge : edges) {
			nodes.add(edge.getSource());
		}

		int idx = nodes.indexOf(edges.get(edges.size() - 1).getTarget());

		while (idx-- > 0)
			edges.remove(0);
	}

	/**
	 * Check if a breadcrum is a cycle
	 * 
	 * @param edges
	 * @return
	 */
	private boolean isCycle(List<E> edges) {
		List<Node> nodes = new ArrayList<>(edges.size());

		for (E edge : edges) {
			nodes.add(edge.getSource());
		}

		return nodes.contains(edges.get(edges.size() - 1).getTarget());
	}

	/**
	 * Return true if we are cycling
	 * 
	 * @param node
	 * @param ctx
	 * @return
	 */
	private void checkCycle(N node, CheckContext ctx, String[] edgeNames) {
		// Find relevant 'next' edges in the graph
		List<E> edges = GraphUtil.getInstance().findEdgesBySourceNode(ctx._edges, node, edgeNames);
		for (E edge : edges)
			ctx._edges.remove(edge);
		for (E edge : edges) {
			// Check if the edge is not already part of another cycle
			boolean found = false;
			if (!ctx._all) {
				for (List<E> cycle : ctx._cycles) {
					for (E ce : cycle) {
						if (ce.equals(edge)) {
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
			}
			if (!found && !ctx._breadcrum.contains(edge)) {
				CheckContext newCtx = ctx.copy();
				// Add the edge to the breadcrum

				newCtx._breadcrum.add(edge);
				// Check if the breadcrum is a cycle
				if (isCycle(newCtx._breadcrum)) {
					// Yep we are cycling
					ctx._cycles.add(newCtx._breadcrum);
				} else {
					// Ha! were not cycling yet : clone the context
					checkCycle(edge.getTarget(), newCtx, edgeNames);
				}
			}
		}
	}

	private class CheckContext {
		List<E> _breadcrum = null;
		List<List<E>> _cycles;
		Graph<N, E> _graph;
		boolean _all;
		List<E> _edges;

		public CheckContext copy() {
			CheckContext result = new CheckContext();

			result._breadcrum = new ArrayList<>();
			result._graph = this._graph;
			for (E edge : this._breadcrum)
				result._breadcrum.add(edge);
			result._edges = this._edges;
			result._cycles = this._cycles;
			result._all = this._all;
			return result;
		}
	}
}
