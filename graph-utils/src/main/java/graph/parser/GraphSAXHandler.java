package graph.parser;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.xml.sax.Attributes;

import csv.CSVData;
import csv.CSVUtil;
import csv.CSVUtil.Option;
import data.ConfigurationUtil;
import data.Filter;
import data.LogUtil;
import graph.GraphConst;
import graph.GraphOption;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.dm.Graph;
import graph.dm.Node;
import xml.XMLSAXHandler;

/**
 * Purpose: SAX Handler for parsing XML information into a graph.
 * 
 * @author mwa17610
 * 
 */
public abstract class GraphSAXHandler<N extends Node, E extends Edge<N>> extends XMLSAXHandler
		implements GraphContentHandler<N, E> {
	/*
	 * Factory for node creation
	 */
	protected GraphHandlerFactory<N, E> factory;
	@SuppressWarnings("rawtypes")
	protected Class[] excludedClasses = new Class[] {};
	/*
	 * The graph to put the nodes and edges in.
	 */
	protected Graph<N, E> graph;
	/*
	 * The first node that is created by this handler
	 */
	protected N firstNode;
	/*
	 * The node stack created by this handler
	 */
	protected Stack<N> stack;
	/*
	 * Node configuration file to be used
	 */
	private String configurationFilename;
	/*
	 * Configuration
	 */
	private CSVData configuration;

	public GraphSAXHandler(Graph<N, E> graph) {
		super();
		this.stack = new Stack<>();
		this.factory = createNodeFactory();
		this.graph = graph;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		super.startElement(uri, localName, qName, atts);
		N node = factory.createNode(uri, localName, qName, atts);

		if (null != node && ignoreClass(node.getClass()))
			node = null;

		if (null != node) {
			if (null == firstNode)
				firstNode = node;
		}
		stack.push(node);
	}

	/**
	 * Determine if a class can be ignored
	 * 
	 * @param cls
	 * @return
	 */
	protected boolean ignoreClass(Class<?> cls) {
		String result = null;
		// get project
		String project = ConfigurationUtil.getInstance().getProjectName();
		project = (!project.isEmpty()) ? "_" + project : project;

		if (null == configuration)
			configuration = CSVUtil.getInstance().readFromFile(getConfigurationFilename(), ';',
					Option.FIRST_ROW_CONTAINS_HEADERS);

		result = configuration.get(GraphConst.CONFIG_COLUMN_CLASS, cls.getName(),
				GraphConst.CONFIG_COLUMN_IGNORE + project);

		return "x".equalsIgnoreCase(result);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		factory.postNodeCreation(getCurrentNode(), uri, localName, qName, data);
		super.endElement(uri, localName, qName);
		if (!stack.isEmpty() && null != stack.peek()) {
			N node = getCurrentNode();
			N parent = findParent(excludedClasses, node);
			if (null != parent) {
				List<E> edges = factory.createEdges(parent, node);

				for (E edge : edges)
					graph.addEdge(edge, GraphOption.CHECK_DUPLICATES);
			}
		}
		stack.pop();
	}

	abstract public GraphHandlerFactory<N, E> createNodeFactory();

	public GraphHandlerFactory<N, E> getNodeFactory() {
		return factory;
	}

	@SuppressWarnings("rawtypes")
	public N findParent(Class[] excludedClasses, Node n) {
		N result = null;
		int t = stack.size();
		int i = t - 1;

		while (i >= 0 && n != stack.get(i--))
			;

		while (i >= 0 && result == null) {
			result = stack.get(i);
			if (i == 0)
				break;
			else if (isOfClass(result, excludedClasses)) {
				result = null;
			}
			--i;
		}

		return result;
	}

	public N getFirstNode() {
		return firstNode;
	}

	/**
	 * Get the current node on the stack that is NOT null.
	 * 
	 * The stack is traversed lifi until the first NON null entry is encountered
	 * 
	 * @return
	 */
	public N getCurrentNode() {
		N result = null;
		int i = stack.size();

		while (i > 0 && null == result)
			result = stack.get(--i);

		return result;
	}

	/**
	 * Filter nodes in the stack (path - hierarchie)
	 * 
	 * @param filter
	 * @return
	 */
	public List<N> filterNodesToRoot(Filter<N> filter) {
		List<N> result = stack.stream().filter(n -> filter.include(n)).collect(Collectors.toList());

		return result;
	}

	/**
	 * Get the root node on the stack that is NOT null.
	 * 
	 * The stack is traversed fili until the first NON null entry is encountered
	 * 
	 * @return
	 */
	public N getRootNode() {
		N result = null;

		int i = 0;
		int max = stack.size();

		while (i < max && null == result)
			result = stack.get(i++);

		if (null == result)
			LogUtil.getInstance()
					.warning("root node is null, check node configuration for exclusions ["
							+ GraphConst.GRAPH_UTIL_NODE_CONFIGURATION + "]=" + ConfigurationUtil.getInstance()
									.getSetting(GraphConst.GRAPH_UTIL_NODE_CONFIGURATION));
		return result;
	}

	public N getParentNode(Class<?> cls) {
		N result = null;
		int max = stack.size();

		while (max > 0) {
			N tmp = stack.get(--max);
			if (null != tmp && cls.isAssignableFrom(tmp.getClass())) {
				result = tmp;
				break;
			}
		}

		return result;
	}

	public Graph<N, E> getGraph() {
		return graph;
	}

	public static class DefaultNodeFactory extends GraphHandlerFactory<Node, Edge<Node>> {
		public DefaultNodeFactory(GraphSAXHandler<Node, Edge<Node>> handler) {
			super(handler);
		}

		@Override
		public Node createNode(String uri, String localName, String qName, Attributes atts) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Edge<Node>> createEdges(Node source, Node target) {
			return createList(new Edge[] { new Edge<>(source, target, EdgeType.HAS) });
		}

		@Override
		public void postNodeCreation(Node currentNode, String uri, String localName, String qName, StringBuilder data) {
		}
	}

	@Override
	public void setConfigurationFilename(String configurationFilename) {
		this.configurationFilename = configurationFilename;

	}

	@Override
	public String getConfigurationFilename() {
		return this.configurationFilename;
	}
}
