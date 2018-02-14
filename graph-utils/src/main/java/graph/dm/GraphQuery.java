package graph.dm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import data.Filter;
import graph.dm.GraphIndex.Direction;

/**
 * Query (directed) graph objects
 * 
 * <pre>
 * A path defines a certain direction in path in a graph and the specific value of the node the path ends in. A direction can either be followed (using outgoing edges) of followed the 
 * opposite way (using incoming edges). The syntax is as follows:
 * > : follow a direction (outgoing edges)
 * < : follow a direction in the opposite way (incoming edges)
 * * : A node class
 * . : Marks the beginning of a node property
 * 
 * Example:
 * >*>Context>Report>LoggingKey.id
 * 	- From the current node
 *  - Follow ALL outgoing edges
 *  - Follow the outgoing edge leading to a Context node
 *  - Follow the outgoing edge leading to a Report node
 *  - Follow the outgoing edge leading to a LoggingKey node
 *  - From the logging key node, take the id property
 *  If following the edges leads to multiple nodes ALL these nodes participate in the subsequent 'follow'. So if in the example there are 
 *  more LoggingKey nodes, the id of all these nodes is concatenated, by using a reduce function (BinaryOperation, GraphQueryNodePropertyMapper).
 * </pre>
 * 
 * @author mwa17610
 *
 * @param <N>
 * @param <E>
 */
public class GraphQuery<N extends Node, E extends Edge<N>> {
	private static final GraphQuery<Node, Edge<Node>> _empty = new GraphQuery<>();
	private GraphIndex<N, E> index;
	private List<N> nodes;

	public GraphQuery() {

	}

	public GraphQuery(GraphIndex<N, E> index, N node) {
		this.index = index;
		if (null != node) {
			this.nodes = new ArrayList<>();
			this.nodes.add(node);
		}
	}

	public GraphQuery(GraphIndex<N, E> index, List<N> nodes) {
		this.index = index;
		this.nodes = nodes;
	}

	/**
	 * Get a new graph query based on a node filter on the target nodes (found on
	 * either the incoming (follow) or the outgoing (opposite) edges).
	 * 
	 * More nodes can be found.
	 * 
	 * @param filter
	 * @param direction
	 * @return
	 */
	protected GraphQuery<N, E> get(GraphFilter<N, E> filter, Direction direction) {
		@SuppressWarnings("unchecked")
		GraphQuery<N, E> result = (GraphQuery<N, E>) _empty;

		if (!isEmpty()) {
			List<N> resultNodes = new ArrayList<>();
			for (N node : nodes) {
				List<E> edges = index.getEdges(node, direction);
				if (null != edges) {
					List<N> filteredNodes = edges.stream()
							.filter(e -> filter.apply(e,
									(direction == Direction.FOLLOW) ? e.getTarget() : e.getSource(), direction))
							.map(e -> (direction == Direction.FOLLOW) ? e.getTarget() : e.getSource())
							.collect(Collectors.toList());
					resultNodes.addAll(filteredNodes);

				}
			}
			if (!resultNodes.isEmpty())
				result = new GraphQuery<>(this.index, resultNodes);
		}

		return result;
	}

	/**
	 * Follow based on GraphFilter
	 * 
	 * @param filter
	 * @return
	 */
	protected GraphQuery<N, E> f(GraphFilter<N, E> filter) {
		return get(filter, Direction.FOLLOW);
	}

	/**
	 * Opposite based on GraphFilter
	 * 
	 * @param filter
	 * @return
	 */
	protected GraphQuery<N, E> o(GraphFilter<N, E> filter) {
		return get(filter, Direction.OPPOSITE);
	}

	/**
	 * Follow based on Node Java-class
	 * 
	 * @param filter
	 * @return
	 */
	public GraphQuery<N, E> f(Class<?> cls) {
		return f((e, n, d) -> n.getClass().isAssignableFrom(cls));
	}

	/**
	 * Opposite based on Node Java-class
	 * 
	 * @param filter
	 * @return
	 */
	public GraphQuery<N, E> o(Class<?> cls) {
		return o((e, n, d) -> n.getClass().isAssignableFrom(cls));
	}

	/**
	 * Follow based on Node Id
	 * 
	 * @param filter
	 * @return
	 */
	public GraphQuery<N, E> f(String id) {
		return f((e, n, d) -> n.getId().equals(id));
	}

	/**
	 * Opposite based on Node Id
	 * 
	 * @param filter
	 * @return
	 */
	public GraphQuery<N, E> o(String id) {
		return o((e, n, d) -> n.getId().equals(id));
	}

	/**
	 * Filter nodes based on Node Java-class
	 * 
	 * @param filter
	 * @return
	 */
	public GraphQuery<N, E> filter(Filter<N> filter) {
		if (!isEmpty()) {
			nodes = nodes.stream().filter(n -> filter.include(n)).collect(Collectors.toList());
		}
		return this;
	}

	public String getByPath(String path) {
		return getByPath(path, (a, b) -> a + b, (n, p) -> n.getProperty(p));
	}

	/**
	 * Combines node values found by a path by using a separator character
	 * 
	 * @param path
	 * @param separatorChar
	 * @return
	 */
	public String getByPath(String path, char separatorChar) {
		return getByPath(path, (a, b) -> a + ((!a.isEmpty()) ? Character.toString(separatorChar) : "") + b,
				(n, p) -> n.getProperty(p));
	}

	/**
	 * Get the value of a node by a path definition.
	 * 
	 * @param path
	 *            node path
	 * @param reduce
	 *            combining multiple properties
	 * @param nodePropertyMapper
	 *            get a specific property
	 * @return
	 */
	public String getByPath(String path, BinaryOperator<String> reduce,
			GraphQueryNodePropertyMapper<N> nodePropertyMapper) {
		GraphQuery<N, E> query = this;
		String result = "";
		int prevIdx = 0;
		int idx;
		class Locals {
			GraphQueryNodePropertyMapper<N> nodePropertyMapper;
		}
		Locals _locals = new Locals();
		_locals.nodePropertyMapper = nodePropertyMapper;
		if (null == _locals.nodePropertyMapper)
			_locals.nodePropertyMapper = (n, p) -> n.getProperty(p);
		do {
			Direction direction = null;

			idx = path.indexOf('>', prevIdx + 1);
			if (idx < 0)
				idx = path.indexOf('<', prevIdx + 1);
			if (idx < 0)
				idx = path.indexOf('.', prevIdx + 1);
			if (idx < 0)
				idx = path.length();
			String part = path.substring(prevIdx, idx);
			switch (part.charAt(0)) {
			case '>':
				direction = Direction.FOLLOW;
				part = part.substring(1);
				break;
			case '<':
				direction = Direction.OPPOSITE;
				part = part.substring(1);
				break;
			case '.':
				part = part.substring(1);
				break;
			}
			prevIdx = idx;

			if (null != direction) {
				String strTest = (part.equals("*")) ? ".*" : part;
				query = query.get((e, n, d) -> ((strTest.charAt(0) == ':' && null != e.getDescription()) ? e.getDescription().matches(strTest.substring(1))
						: n.getClass().getSimpleName().matches(strTest + "(Node)?")), direction);
			} else {
				String strTest = part;
				result = query.get(n -> _locals.nodePropertyMapper.map(n, strTest), reduce);
				query = null;
			}
		} while (null != query && !query.isEmpty());

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends N> T get() {
		if (null != nodes && !nodes.isEmpty())
			return (T) nodes.get(0);
		else
			return null;
	}

	/**
	 * Gets a value for a node
	 * 
	 * @param map
	 * @param reduce
	 * @return
	 */
	public String get(Function<N, String> map, BinaryOperator<String> reduce) {
		String result = "";
		if (!isEmpty())
			result = nodes.stream().map(n -> map.apply(n)).filter(str -> null != str && !str.isEmpty())
					.collect(Collectors.toMap(str -> str, str -> str, (str1, str2) -> str1)).keySet().stream()
					.reduce("", (a, b) -> reduce.apply(a, b));

		return result;
	}

	public boolean isEmpty() {
		return this.nodes == null || this.nodes.isEmpty();
	}
}
