package graph.dm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import data.EnumUtil;
import data.Filter;
import graph.GraphOption;
import object.ObjectUtil;

/**
 * <pre>
 * Purpose: Stores nodes and connection between them (edges). Object models and	
 *          relational models can easily be represented by graphs. F.e. references 
 *          from objects to other objects in an object models and relations defined 
 *          between entities in a relational model are typically expressed by using 
 *          edges in a graph model. Because of it more generic nature, a graph representation 
 *          allows for typically graph like algorithms like shortest path.
 * 
 * </pre>
 * 
 * @author mwa17610
 * 
 */
public class Graph<N extends Node, E extends Edge<N>> {
	private String name;
	private List<N> nodes;
	private List<E> edges;

	{
		_init();
	}

	public Graph() {
		super();
	}

	private void _init() {
		nodes = new ArrayList<>();
		edges = new ArrayList<>();
	}

	public void addEdge(E edge, GraphOption... options) {
		addEdge(edge, null, options);
	}

	public void addEdge(E edge, Comparator<N> comparator, GraphOption... options) {
		if (EnumUtil.getInstance().contains(options, GraphOption.CHECK_DUPLICATES)) {
			N existingSourceNode = find(edge.getSource(), comparator);
			if (null != existingSourceNode)
				edge.setSource(ObjectUtil.getInstance().merge(edge.getSource(), existingSourceNode, Node.class));
			else
				nodes.add(edge.getSource());
			N existingTargetNode = find(edge.getTarget(), comparator);
			if (null != existingTargetNode)
				edge.setTarget(ObjectUtil.getInstance().merge(edge.getTarget(), existingTargetNode, Node.class));
			else
				nodes.add(edge.getTarget());
			boolean edgeExists = false;
			// try to find the edge if source and target are both found
			if (null != existingSourceNode && null != existingTargetNode) {
				List<E> edges = filterEdges(e -> e.getSource() == edge.getSource() && e.getTarget() == edge.getTarget()
						&& e.getName().equals(edge.getName()));
				if (!edges.isEmpty())
					edgeExists = true;
			}
			// only add the edge if it does not already exist
			if (!edgeExists)
				edges.add(edge);
		} else {
			nodes.add(edge.getSource());
			nodes.add(edge.getTarget());
			edges.add(edge);
		}
	}

	public List<N> getNodes() {
		return nodes;
	}

	public List<E> getEdges() {
		return edges;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Find connections between source and target. Depth = 1
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public List<E> filter(N source, N target) {
		List<E> result = filterEdges(e -> e.getSource().treatAsSame(source) && e.getTarget().treatAsSame(target));

		return result;
	}

	/**
	 * Find the edges of a graph according to filter
	 * 
	 * @param filter
	 * 
	 * @return
	 */
	public List<E> filterEdges(Filter<E> filter) {
		List<E> result = new ArrayList<>();

		for (E edge : edges) {
			if (filter.include(edge)) {
				result.add(edge);
			}
		}

		return result;
	}

	/**
	 * Find the edges of a graph according to filter
	 * 
	 * @param filter
	 * 
	 * @return
	 */
	public List<N> filterNodes(Filter<N> filter) {
		List<N> result = new ArrayList<>();

		for (N node : nodes) {
			if (filter.include(node)) {
				result.add(node);
			}
		}

		return result;
	}

	/**
	 * Appends a graph to this graph. Depending on the 'check' a node is only
	 * inserted if it does not already exist in this graph (e.g. Node::equals()
	 * returns false)
	 * 
	 * @param graph
	 * @param check
	 */
	public <AN extends Node, AE extends Edge<AN>> void append(Graph<AN, AE> graph, GraphOption... options) {
		append(graph, null, options);
	}

	/**
	 * append a graph to this graph,
	 * 
	 * <pre>
	 * 	NOTE checkDuplicates = FALSE => unconnected	nodes are NOT appended
	 * </pre>
	 * 
	 * 
	 * @param graph
	 * @param comparator
	 * @param edgeFactory
	 * @param options
	 */
	@SuppressWarnings("unchecked")
	public <AN extends Node, AE extends Edge<AN>> void append(Graph<AN, AE> graph, Comparator<N> comparator,
			GraphOption... options) {
		if (null != graph) {
			// Add all unconnected nodes
			// @ToDo currently only works if checkDuplicates is TRUE, if
			// checkDuplicates = FALSE might not
			// work as expected
			if (EnumUtil.getInstance().contains(options, GraphOption.CHECK_DUPLICATES) || graph.getEdges().isEmpty()) {
				for (AN node : graph.getNodes()) {
					addNode((N) node, comparator, options);
				}
			}
			for (AE edge : graph.getEdges()) {
				addEdge((E) edge, comparator, options);
			}
		}
	}

	/**
	 * Appends a graph to this graph. Depending on the 'check' a node is only
	 * inserted if it does not already exist in this graph (e.g.
	 * Comparator.compare() returns 0 (zero))
	 * 
	 * @param graph
	 * @param check
	 */
	public <AN extends Node, AE extends Edge<AN>> void append(Graph<AN, AE> graph, Comparator<N> comparator) {
		append(graph, comparator, GraphOption.CHECK_DUPLICATES);
	}

	/**
	 * Get the index of a node in the node list
	 * 
	 * @param node
	 * @return
	 */
	public int indexOf(N node, Comparator<N> comparator) {
		int result = 0;

		for (N n : nodes) {
			if ((null != comparator && 0 == comparator.compare(n, node)) || (null == comparator && n.treatAsSame(node)))
				break;
			++result;
		}
		if (result >= nodes.size())
			result = -1;

		return result;
	}

	/**
	 * Checks if a node exists.
	 * 
	 * @param node
	 * @return
	 */
	public boolean exists(N node, Comparator<N> comparator) {
		return null != find(node, comparator);
	}

	/**
	 * Returns the actual node in the node list
	 * 
	 * @param node
	 * @return
	 */
	public N find(N node, Comparator<N> comparator) {
		N result = null;
		boolean slow = true;
		/**
		 * The parallelstream option is NOT faster then the iterative approach
		 * (5 seconds - 8 seconds), so we set SLOW=TRUE, because we assumed
		 * iteration would be slower.
		 */
		if (slow) {
			for (N n : nodes)
				if ((null != comparator && 0 == comparator.compare(n, node))
						|| (null == comparator && n.treatAsSame(node))) {
					result = n;
					break;
				}
		} else {
			Optional<N> optNode = nodes.parallelStream()
					.filter(n -> (null != comparator && 0 == comparator.compare(n, node))
							|| (null == comparator && n.treatAsSame(node)))
					.findAny();

			if (optNode.isPresent())
				result = optNode.get();
		}
		return result;
	}

	public void addNode(N node, GraphOption... options) {
		addNode(node, null, options);
	}

	/**
	 * Add a node to the graph
	 * 
	 * @param node
	 * @param checkDuplicates
	 * @param comparator
	 */
	public void addNode(N node, Comparator<N> comparator, GraphOption... options) {

		N existingNode = find(node, comparator);
		if (null == existingNode)
			nodes.add(node);
		else
			ObjectUtil.getInstance().merge(node, existingNode, Node.class);

	}

	/**
	 * Get the graph limited by the nodes in the cluster
	 * 
	 * @param cluster
	 * @param recursive
	 * @param checkDuplicates
	 * @return
	 */
	public Graph<N, E> getGraphByCluster(Cluster<N> cluster, GraphOption... options) {
		return getGraphByCluster(cluster, null, options);
	}

	/**
	 * Get the graph limited by the nodes in the cluster
	 * 
	 * @param cluster
	 * @param recursive
	 * @param checkDuplicates
	 * @param comparator
	 * @return
	 */
	public Graph<N, E> getGraphByCluster(Cluster<N> cluster, Comparator<N> comparator, GraphOption... options) {
		Graph<N, E> result = new Graph<>();

		for (E edge : getEdges()) {
			if (cluster.contains(edge.getSource(), options) && cluster.contains(edge.getTarget(), options)) {
				result.addEdge(edge, comparator, options);
			}
		}
		for (N node : getNodes()) {
			if (cluster.contains(node, options))
				result.addNode(node, comparator, options);
		}

		return result;
	}

	public Graph<N, E> shallowCopy() {
		Graph<N, E> gra = new Graph<>();

		gra.name = this.name;
		for (E edge : this.edges)
			gra.edges.add(edge);
		for (N node : this.nodes)
			gra.nodes.add(node);

		return gra;

	}

	public void removeEdge(E edge) {
		edges.remove(edge);
	}

	/**
	 * Will also remove all edges within which this node is contained
	 * 
	 * @param node
	 */
	public void removeNode(N node) {
		List<E> edges = new ArrayList<>();

		for (E edge : filterEdges(e -> e.getSource().equals(node) || e.getTarget().equals(node)))
			edges.add(edge);
		for (E edge : edges)
			removeEdge(edge);

		nodes.remove(node);
	}
}
