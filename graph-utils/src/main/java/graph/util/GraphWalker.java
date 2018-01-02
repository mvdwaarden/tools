package graph.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;

public class GraphWalker<N extends Node, E extends Edge<N>> implements Iterator<E> {
	Graph<N, E> graph;
	GraphWalkerCallback<N, E> walker;
	List<E> walked;
	N root;
	List<E> current;

	public GraphWalker(Graph<N, E> graph, GraphWalkerCallback<N, E> walker, N root) {
		this.graph = graph;
		this.walker = walker;
		this.root = root;
		this.walked = new ArrayList<>();
		_init();
	}

	public GraphWalker(Graph<N, E> graph, GraphWalkerCallback<N, E> walker) {
		this.graph = graph;
		this.walker = walker;
		List<E> edges = graph.getEdges();

		if (!edges.isEmpty())
			this.root = edges.get(0).getSource();
		else
			this.root = null;
		this.walked = new ArrayList<>();
		_init();
	}

	public GraphWalker(Graph<N, E> graph) {
		this(graph, null);
	}

	private void _init() {
		current = GraphUtil.getInstance().findEdgesBySourceNode(graph.getEdges(), root);
	}

	public void walk() {
		while (hasNext())
			next();
	}

	@Override
	public boolean hasNext() {
		return !current.isEmpty();
	}

	@Override
	public E next() {
		E result = current.get(0);

		if (null != walker)
			walker.walk(result);
		walked.add(result);
		current.remove(0);
		List<E> nextEdges = GraphUtil.getInstance().findEdgesBySourceNode(graph.getEdges(), result.getTarget());

		for (E edge : nextEdges) {
			if (!walked.contains(edge) && !current.contains(edge))
				current.add(edge);
		}

		return result;
	}

	@Override
	public void remove() {

	}

}
