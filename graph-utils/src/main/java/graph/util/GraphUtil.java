package graph.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import data.ConfigurationUtil;
import data.DataUtil;
import data.Filter;
import data.Util;
import graph.GraphConst;
import graph.GraphException;
import graph.GraphOption;
import graph.dm.Cluster;
import graph.dm.ClusterNode;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import graph.parser.EdgeFactory;
import graph.parser.GraphContentHandler;
import metadata.MetaAtom.BaseType;
import metadata.MetaComposite;
import metadata.MetaData;
import metadata.MetaElement;
import metadata.MetaRelation;
import metadata.MetaType;
import object.ObjectIterator;
import object.ObjectUtil;
import xml.XMLUtil;

/**
 * Purpose: Utility class for graph package
 * 
 * @author mwa17610;
 * 
 */
public class GraphUtil implements Util {
	private static ThreadLocal<GraphUtil> instance = new ThreadLocal<GraphUtil>();

	/**
	 * Read XML content using a SAXHandler
	 * 
	 * @param sourcedir
	 * @param file
	 * @param handler
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N, E> read(String baseUrl, String file,
			GraphContentHandler<N, E> handler) {
		String fileUrl = DataUtil.getInstance().convertToFileURL(file);
		SAXParser parser = null;
		// check if there is a file specific configuration XML
		String name = DataUtil.getInstance().getFilenameWithoutExtension(file);
		String nodeconfig = ConfigurationUtil.getInstance()
				.getSetting(GraphConst.GRAPH_UTIL_NODE_CONFIGURATION + "." + name);
		if (null == nodeconfig || nodeconfig.isEmpty())
			nodeconfig = ConfigurationUtil.getInstance().getSetting(GraphConst.GRAPH_UTIL_NODE_CONFIGURATION);

		try {
			handler.setSourceUrl(DataUtil.getInstance().convertToFileURL(fileUrl));
			handler.setConfigurationFilename(nodeconfig);
			handler.setBaseUrl(baseUrl);
			parser = XMLUtil.getInstance().getSaxParser();
			parser.getXMLReader().setContentHandler(handler);
			handler.setXMLReader(parser.getXMLReader());
			
			InputSource is = new InputSource(fileUrl);
			handler.setInputSource(is);
			parser.getXMLReader().parse(is);
			N first = handler.getFirstNode();

			if (null != first && null == first.getId()) {
				first.setId(name);
				if (null == first.getName())
					first.setName(first.getId());
				if (null == first.getDescription())
					first.setDescription(first.getId());

			}
			if (null != first && null != handler.getGraph() && handler.getGraph().getNodes().isEmpty())
				handler.getGraph().getNodes().add(first);

		} catch (SAXException e) {
			throw new GraphException("check SAX configuration [" + fileUrl + "]", e);
		} catch (IOException e) {
			throw new GraphException("file [" + fileUrl + "] not found", e);			
		} finally {

		}

		return handler.getGraph();
	}

	/**
	 * Converts an object in to a hashmap of key value pairs
	 * 
	 * Referenced objects are NOT converted
	 * 
	 * @param node
	 * @param baseClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> object2NV(Object node, Class baseClass) {
		return ObjectUtil.getInstance().object2NV(node, baseClass, v -> null != v && isValidValue(v));
	}

	/**
	 * Checks if the object is a valid value.
	 * 
	 * Note: Currently there is no risk of endless loops because only simple
	 * values are allowed (primitives and array of primitives)
	 * 
	 * @param value
	 * @return
	 * 
	 * @see #object2NV(Object, Class)
	 */
	private boolean isValidValue(Object value) {
		return isSimpleValue(value) || isListOfSimpleValues(value);
	}

	/**
	 * Checks if the object is a simple value.
	 * 
	 * @param value
	 * @return
	 * 
	 * @see #isValidValue(Object, Class)
	 */
	private boolean isSimpleValue(Object value) {
		return value instanceof String || value instanceof Integer || value instanceof Long || value instanceof Date
				|| value instanceof Double || value instanceof Float || value instanceof Enum
				|| value instanceof Boolean;
	}

	/**
	 * Checks if the object is a list of valid simple values.
	 * 
	 * @param value
	 * @return
	 * 
	 * @see #isValidValue(Object, Class)
	 */
	@SuppressWarnings("rawtypes")
	private boolean isListOfSimpleValues(Object value) {
		return value instanceof List && !((List) value).isEmpty() && isSimpleValue(((List) value).get(0));
	}

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static GraphUtil getInstance() {
		GraphUtil result = instance.get();

		if (null == result) {
			result = new GraphUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * Find edges depending on the source node
	 * 
	 * @param edges
	 * @param node
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> List<E> findEdgesBySourceNode(List<E> edges, N node) {
		return findEdgesBySourceNode(edges, node, null);
	}

	/**
	 * Find edges depending on the source node, filter on the name of the edge.
	 * 
	 * If the edgeNames are empty then NOTHING is filtered
	 * 
	 * @param edges
	 * @param node
	 * @param edgeNames
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> List<E> findEdgesBySourceNode(List<E> edges, N node,
			String[] edgeNames) {
		return findEdgesByNode(edges, node, edgeNames, e -> e.getSource());
	}

	/**
	 * Find edges depending on the target node, filter on the name of the edge.
	 * 
	 * If the edgeNames are empty then NOTHING is filtered
	 * 
	 * @param edges
	 * @param node
	 * @param edgeNames
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> List<E> findEdgesByTargetNode(List<E> edges, N node,
			String[] edgeNames) {
		return findEdgesByNode(edges, node, edgeNames, e -> e.getTarget());
	}

	/**
	 * Find edges depending on a node.
	 * 
	 * If the edgeNames are empty then NOTHING is filtered
	 * 
	 * @param edges
	 * @param node
	 * @param edgeNames
	 * @return
	 */
	protected <N extends Node, E extends Edge<N>> List<E> findEdgesByNode(List<E> edges, N node, String[] edgeNames,
			Function<E, N> selector) {
		List<E> result = new ArrayList<>();

		for (E edge : edges) {
			if (selector.apply(edge).treatAsSame(node)) {
				if (null == edgeNames || edgeNames.length == 0)
					result.add(edge);
				else {
					boolean allowedEdge = false;
					for (String allowedName : edgeNames) {
						if (edge.getName().matches(allowedName)) {
							allowedEdge = true;
						}
					}
					if (allowedEdge)
						result.add(edge);
				}
			}
		}

		return result;
	}

	/**
	 * Find the roots from a graph, i.e. no 'incoming' relations
	 * 
	 * @param graph
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> List<N> findRoots(Graph<N, E> graph) {
		List<N> result = new ArrayList<>();
		Map<N, Integer> nfoRefcount = getNodeReferenceCount(graph, null);

		for (Entry<N, Integer> e : nfoRefcount.entrySet()) {
			if (e.getValue() == 0)
				result.add(e.getKey());
		}

		return result;
	}

	/**
	 * Get the reference count for each node
	 * 
	 * @param graph
	 * @param edgeName
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Map<N, Integer> getNodeReferenceCount(Graph<N, E> graph,
			String edgeName) {
		Map<N, Integer> result = new HashMap<>();

		for (E edge : graph.getEdges()) {
			if (null == edgeName || edge.getName().equals(edgeName)) {
				Integer count = result.get(edge.getTarget());
				if (null == count)
					result.put(edge.getTarget(), 1);
				else
					result.put(edge.getTarget(), ++count);
			}
		}

		for (N node : graph.getNodes()) {
			if (null == result.get(node)) {
				result.put(node, 0);
			}
		}

		return result;
	}

	/**
	 * Get the reference count for each node
	 * 
	 * @param graph
	 * @param start
	 *            nodes
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N, E> getGraphByStartNodes(Graph<N, E> graph, List<N> startNodes) {
		Graph<N, E> result = new Graph<>();

		Map<E, Boolean> edges = new HashMap<>();
		// Walk the graph for each start node and add every 'encountered' edge
		for (N node : startNodes) {
			GraphWalker<N, E> walker = new GraphWalker<N, E>(graph, (E e) -> {
				if (null == edges.get(e))
					edges.put(e, true);
			}, node);
			walker.walk();
		}
		result.setName(graph.getName());
		for (E edge : edges.keySet())
			result.addEdge(edge, GraphOption.CHECK_DUPLICATES);

		return result;
	}

	/**
	 * Filter a graph based on a filter
	 * 
	 * @param graph
	 * @param filter
	 * @param checkDuplicates
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N, E> filter(Graph<N, E> graph, Filter<E> filter,
			GraphOption... options) {
		return filter(graph, filter, null, options);
	}

	/**
	 * Filter a graph based on a filter
	 * 
	 * @param graph
	 * @param filter
	 * @param checkDuplicates
	 * @param comparator
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N, E> filter(Graph<N, E> graph, Filter<E> filter,
			Comparator<N> comparator, GraphOption... options) {
		Graph<N, E> result = new Graph<>();
		result.setName(graph.getName());
		for (E edge : graph.getEdges()) {
			if (filter.include(edge)) {
				result.addEdge(edge, comparator, options);
			}
		}

		return result;
	}

	/**
	 * Get a graph by a node.
	 * 
	 * First node in the node list is ALWAYS the root.
	 * 
	 * @param root
	 * @param comparator
	 * @param options
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N, E> getGraphByNode(Graph<N, E> graph, N root,
			Comparator<N> comparator, GraphOption... options) {
		Graph<N, E> result = new Graph<>();
		result.addNode(root, comparator, options);
		List<E> usedEdge = new ArrayList<>();
		List<E> roads = GraphUtil.getInstance().findEdgesBySourceNode(graph.getEdges(), root);

		while (!roads.isEmpty()) {
			E road = roads.get(0);
			result.addEdge(road, comparator, options);
			usedEdge.add(road);
			List<E> newRoads = GraphUtil.getInstance().findEdgesBySourceNode(graph.getEdges(), road.getTarget());

			for (E newRoad : newRoads) {
				if (!usedEdge.contains(newRoad)) {
					roads.add(newRoad);
				}
			}
			roads.remove(0);
		}

		return result;
	}

	/**
	 * Get a graph by a node.
	 * 
	 * First node in the node list is ALWAYS the root.
	 * 
	 * @param root
	 * @param options
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N, E> getGraphByNode(Graph<N, E> graph, N root,
			GraphOption... options) {
		return getGraphByNode(graph, root, null, options);
	}

	/**
	 * Create a graph for connected clusters. A cluster is connected to another
	 * cluster when at least one of the nodes in the cluster is connected to at
	 * least one of the nodes in the other cluster.
	 * 
	 * The nodes in the resulting graph are of type ClusterNodes
	 * 
	 * @param gra
	 * @param clusters
	 * @param edgeName
	 * @param comparator
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<ClusterNode<N>, Edge<ClusterNode<N>>> createGraphForClusters(
			Graph<N, E> gra, List<Cluster<N>> clusters, String edgeName, Comparator<ClusterNode<N>> comparator) {
		Graph<ClusterNode<N>, Edge<ClusterNode<N>>> result = new Graph<>();
		List<E> edges = gra.getEdges();
		for (Cluster<N> clusterA : clusters) {
			List<N> nodesA = clusterA.getNodes();
			clusterConnection: for (Cluster<N> clusterB : clusters) {
				List<N> nodesB = clusterB.getNodes();
				if (!clusterA.equals(clusterB)) {
					for (E edge : edges) {
						if (nodesA.contains(edge.getSource()) && nodesB.contains(edge.getTarget())) {
							ClusterNode<N> clusterSource = new ClusterNode<N>();
							clusterSource.setId(clusterA.getName());
							clusterSource.setDescription(clusterA.getDescription());
							clusterSource.setCluster(clusterA);
							ClusterNode<N> clusterTarget = new ClusterNode<N>();
							clusterTarget.setId(clusterB.getName());
							clusterTarget.setDescription(clusterB.getDescription());
							clusterTarget.setCluster(clusterB);
							Edge<ClusterNode<N>> clusterEdge = new Edge<>();
							clusterEdge.setName(edge.getSource().getId() + "->" + edge.getTarget().getId());
							clusterEdge.setDescription(edgeName);
							clusterEdge.setSource(clusterSource);
							clusterEdge.setTarget(clusterTarget);
							result.addEdge(clusterEdge, comparator, GraphOption.CHECK_DUPLICATES);

							continue clusterConnection;
						}
					}
				}
			}
		}
		// Add all clusters which are NOT connected
		for (Cluster<N> cluster : clusters) {
			ClusterNode<N> clusternode = new ClusterNode<N>();
			clusternode.setId(cluster.getName());
			clusternode.setDescription(cluster.getDescription());
			clusternode.setCluster(cluster);
			result.addNode(clusternode, comparator, GraphOption.CHECK_DUPLICATES);
		}

		return result;
	}

	/**
	 * Create a graph for connected clusters. A cluster is connected to another
	 * cluster when at least one of the nodes in the cluster is connected to at
	 * least one of the nodes in the other cluster.
	 * 
	 * The nodes in the resulting graph are of type ClusterNodes
	 * 
	 * @param gra
	 * @param clusters
	 * @param edgeName
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<ClusterNode<N>, Edge<ClusterNode<N>>> createGraphForClusters(
			Graph<N, E> gra, List<Cluster<N>> clusters, String edgeName) {
		return createGraphForClusters(gra, clusters, edgeName, null);
	}

	/**
	 * Check if the node is related
	 * 
	 * @param node
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> boolean isNodeRelated(List<E> edges, N node) {
		return !findEdgesBySourceNode(edges, node).isEmpty() || !findEdgesByTargetNode(edges, node, null).isEmpty();

	}

	/**
	 * Divide a cluster in subclusters based on roots in the cluster
	 * 
	 * @param gra
	 * @param cluster
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> List<Cluster<N>> createClustersForRoots(Graph<N, E> gra,
			Cluster<N> cluster) {
		List<Cluster<N>> result = new ArrayList<>();
		System.out.println("cluster[" + cluster.getDescription() + "]");
		Graph<N, E> subgraph = gra.getGraphByCluster(cluster, GraphOption.CHECK_DUPLICATES);

		List<N> roots = findRoots(subgraph);
		if (!roots.isEmpty()) {
			for (N node : roots) {
				Cluster<N> rootcluster = new Cluster<N>();
				rootcluster.setDescription(cluster.getDescription());
				rootcluster.setName(cluster.getName() + ":" + node.getId());
				Graph<N, E> rootgraph = getGraphByNode(subgraph, node, GraphOption.CHECK_DUPLICATES);
				rootcluster.addNodes(rootgraph);
				result.add(rootcluster);
			}
		} else {
			result.add(cluster);
		}

		return result;
	}

	/**
	 * Create clusters based on a custom comparison
	 * 
	 * @param gra
	 * @param comparator
	 * @return
	 */

	public <N extends Node, E extends Edge<N>, K> List<Cluster<N>> createClusters(Graph<N, E> gra,
			Function<N, K> mapper) {
		List<Cluster<N>> result = new ArrayList<>();

		Map<K, Cluster<N>> map = new HashMap<>();
		for (N n : gra.getNodes()) {
			K k = mapper.apply(n);
			Cluster<N> c = map.get(k);
			if (null == c) {
				c = new Cluster<N>();
				c.setName(k.toString());
				c.setDescription(k.toString());
				map.put(k, c);
				result.add(c);
			}
			c.addNode(n);
		}

		return result;
	}

	public String makeLabelName(String label) {
		String result = label;
		if (result.endsWith("Node") && result.length() > "Node".length())
			result = result.substring(0, result.length() - "Node".length());

		int idx = result.lastIndexOf(".");
		if (idx >= 0)
			result = result.substring(idx + 1);

		return result;
	}

	public <N extends Node, E extends Edge<N>> MetaData createMetaData(Graph<N, E> gra) {
		MetaData result = new MetaData();

		Map<Class<?>, Boolean> classes = new HashMap<>();

		// Determine the node types
		for (N node : gra.getNodes()) {
			if (null == classes.get(node.getClass()))
				classes.put(node.getClass(), true);
		}
		// Create elements for each node type
		for (Class<?> cls : classes.keySet()) {
			MetaComposite type = new MetaComposite();
			type.setName(makeLabelName(cls.getSimpleName()));
			ObjectIterator it = new ObjectIterator(cls, Node.class);
			it.moveFirst();
			while (it.hasNext()) {
				Field field = it.nextField();
				MetaElement el = new MetaElement();
				el.setName(field.getName());
				el.setType(BaseType.STRING);
				type.addElement(el);
			}
			MetaElement root = new MetaElement();
			root.setName(makeLabelName(cls.getSimpleName()));
			root.setType(type);
			result.addType(type);
			result.addRoot(root);
		}
		// Determine unique relation types
		Map<String, E> relations = new HashMap<>();
		for (E edge : gra.getEdges()) {
			String key = edge.getSource().getClass().getName() + "->" + edge.getTarget().getClass().getName();

			if (null == relations.get(key))
				relations.put(key, edge);
		}

		for (Entry<String, E> e : relations.entrySet()) {
			MetaRelation relation = new MetaRelation(e.getKey());
			MetaElement source = result
					.findElementsByName(makeLabelName(e.getValue().getSource().getClass().getSimpleName())).get(0);
			MetaElement target = result
					.findElementsByName(makeLabelName(e.getValue().getTarget().getClass().getSimpleName())).get(0);
			String name = e.getValue().getName().toLowerCase() + "_" + target.getType().getName();

			if (null == source.getType().getElementByName(name)) {
				MetaElement el = new MetaElement();
				el.setName(name);
				el.setType(target.<MetaType> getType());
				el.setMaxAantal(-1);
				source.getType().getElements().add(el);
			}
			result.addRelation(relation);
		}

		return result;
	}

	public <N extends Node, E extends Edge<N>, R> Graph<Node, Edge<Node>> createMetaGraph(MetaData metadata) {
		Graph<Node, Edge<Node>> result = new Graph<>();
		result.setName(metadata.getRootTag());

		for (MetaElement parent : metadata.getRoots()) {
			for (MetaElement child : parent.getType().getElements()) {
				addElement(result, parent, child);
			}
		}

		return result;
	}

	private void addElement(Graph<Node, Edge<Node>> gra, MetaElement parent, MetaElement child) {
		if (!child.getType().getElements().isEmpty()) {
			Node source = new Node();
			source.setId(parent.getName());
			source.setDescription(parent.getName());
			source.setName(parent.getName());
			Node target = new Node();
			target.setId(child.getName());
			target.setDescription(child.getName());
			target.setName(child.getName());
			gra.addEdge(new Edge<>(source, target, "HAS"), GraphOption.CHECK_DUPLICATES);
			for (MetaElement el : child.getType().getElements()) {
				addElement(gra, child, el);
			}
		}
	}

	public <N extends Node, E extends Edge<N>, R> Graph<Node, Edge<Node>> createMetaGraph(Graph<N, E> gra) {
		return createMetaGraph(gra, n -> n.getClass().getName());
	}

	/**
	 * Creates a metagraph based on a graph.
	 * 
	 * The meta elements are based on a mapper function. The result of the
	 * mapper function creates a unique key, which is used to create the meta
	 * nodes.
	 * 
	 * <pre>
	 * For example, notation : [node type]([instance number]), so a(1) is instance number 1 of node of type a
	 * 	a(1) -> b(1);
	 *  b(1) -> c(1);
	 *  c(1) -> a(2);
	 *  a(1) -> a(2);
	 *  a(1) -> b(2);
	 *  b(2) -> c(1);
	 * 
	 * Suppose mapper function [node type]([instance number]) => [node type] then the resulting graph would be
	 * 	a -> b;
	 *  b -> c;
	 *  c -> a;
	 *  a -> a;
	 * </pre>
	 * 
	 * @param gra
	 * @param mapper
	 * @return
	 */
	public <N extends Node, E extends Edge<N>, R> Graph<Node, Edge<Node>> createMetaGraph(Graph<N, E> gra,
			Function<N, R> mapper) {
		Graph<Node, Edge<Node>> result = new Graph<>();
		result.setName(gra.getName() + "-meta");

		Map<R, N> metanodes = new HashMap<>();

		// Determine the node types
		for (N node : gra.getNodes()) {
			R key = mapper.apply(node);
			if (null == metanodes.get(key))
				metanodes.put(key, node);
		}
		// Create elements for each node type
		for (Entry<R, N> e : metanodes.entrySet()) {
			Node node = new Node();
			node.setId(e.getKey().toString());
			node.setName(e.getKey().toString());
			node.setDescription(makeLabelName(e.getKey().toString()));
			result.addNode(node);
		}
		// Determine unique relation types
		Map<String, E> relations = new HashMap<>();
		for (E edge : gra.getEdges()) {
			String key = mapper.apply(edge.getSource()).toString() + "->" + mapper.apply(edge.getTarget()).toString();
			if (null == relations.get(key))
				relations.put(key, edge);
		}
		// Add relations
		for (Entry<String, E> e : relations.entrySet()) {
			Node source = new Node();
			source.setId(mapper.apply(e.getValue().getSource()).toString());
			Node target = new Node();
			target.setId(mapper.apply(e.getValue().getTarget()).toString());
			Edge<Node> edge = new Edge<>();
			edge.setSource(source);
			edge.setTarget(target);
			result.addEdge(edge, GraphOption.CHECK_DUPLICATES);
		}

		return result;
	}

	/**
	 * Compare two graphs
	 * 
	 * @param gra1
	 * @param gra2
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<Node, Edge<Node>> graphCompare(Graph<N, E> gra1,
			Graph<N, E> gra2) {
		return graphCompare(gra1, gra2, null, null);
	}

	/**
	 * Compare two graphs
	 * 
	 * @param gra1
	 * @param gra2
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <N extends Node, E extends Edge<N>> Graph<Node, Edge<Node>> graphCompare(Graph<N, E> gra1, Graph<N, E> gra2,
			Comparator<E> edgeComparator, Comparator<N> nodeComparator) {
		final Comparator<N> nodeIsEqual = (null != nodeComparator) ? nodeComparator : new Comparator<N>() {
			public int compare(N n1, N n2) {
				return n1.treatAsSame(n2) ? 0 : -1;

			}
		};
		final Comparator<E> edgeIsEqual = (null != edgeComparator) ? edgeComparator : new Comparator<E>() {
			@Override
			public int compare(E e1, E e2) {
				return (0 == nodeIsEqual.compare(e1.getSource(), e2.getSource())
						&& 0 == nodeIsEqual.compare(e1.getTarget(), e2.getTarget())
						&& e1.getName().equals(e2.getName())) ? 0 : -1;
			}
		};
		Graph<Node, Edge<Node>> result = new Graph<>();
		List<N> addedNodes = gra2.filterNodes(n2 -> !gra1.exists(n2, nodeIsEqual));
		List<N> deletedNodes = gra1.filterNodes(n1 -> !gra2.exists(n1, nodeIsEqual));
		List<E> addedEdges = gra2.filterEdges(e2 -> gra1.filterEdges(e1 -> 0 == edgeIsEqual.compare(e1, e2)).isEmpty());
		List<E> deletedEdges = gra1
				.filterEdges(e1 -> gra2.filterEdges(e2 -> 0 == edgeIsEqual.compare(e1, e2)).isEmpty());

		Node added = new Node();
		added.setId(getClass().getName() + ":COMPARE_ADDED");
		added.setDescription("ADDED");
		added.setName("ADDED");
		Node deleted = new Node();
		deleted.setId(getClass().getName() + ":COMPARE_DELETED");
		deleted.setDescription("DELETED");
		deleted.setName("DELETED");
		for (N a : addedNodes)
			result.addEdge(new Edge(added, a, "ADDED", "ADDED"), GraphOption.CHECK_DUPLICATES);
		for (N d : deletedNodes)
			result.addEdge(new Edge(deleted, d, "DELETED", "DELETED"), GraphOption.CHECK_DUPLICATES);
		for (E ae : addedEdges)
			result.addEdge(new Edge(ae.getSource(), ae.getTarget(), ae.getName(), "ADDED"),
					GraphOption.CHECK_DUPLICATES);
		for (E de : deletedEdges)
			result.addEdge(new Edge(de.getSource(), de.getTarget(), de.getName(), "DELETED"),
					GraphOption.CHECK_DUPLICATES);

		return result;
	}
	
	/**
	 * Get dependency graph
	 * @param node
	 * @param map
	 * @param edgeFactory
	 * @return
	 */
	public <N extends Node, E extends Edge<N>> Graph<N,E> getDependencyGraph(N node,Function<N,List<N>> map,EdgeFactory<N,E> edgeFactory){
		Graph<N,E> result = new Graph<>();
		
		for (N n : map.apply(node)){
			// recursion
			if (!result.exists(n,null)){
				Graph<N,E> dep = getDependencyGraph(n,map,edgeFactory);
				result.append(dep,GraphOption.CHECK_DUPLICATES);
			}
			for (E edge : edgeFactory.createEdges(node,n))
				result.addEdge(edge,GraphOption.CHECK_DUPLICATES);
		}
		
		return result;
	}
}
