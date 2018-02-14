package graph.io;

import java.util.List;
import java.util.Map;

import data.EnumUtil;
import graph.dm.Cluster;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.NameDescription;
import graph.dm.Node;
import graph.util.GraphUtil;

/**
 * Purpose: Writes graph information into a 'dotty' file for visual
 * presentation. The node name is used as a label.
 * 
 * DOT is a simple file format for specifying graphs.
 * 
 * @author mwa17610
 * 
 */
public class SimpleDottyWriter<N extends Node, E extends Edge<N>> {
	public enum Option {
		NOP, GRAPH_OPTION_LEFT_TO_RIGHT, GRAPH_OPTION_TOP_TO_BOTTOM, GRAPH_OPTION_GRAY, GRAPH_OPTION_CLUSTER_INCLUDE_RELATED_NODES_ONLY, GRAPH_OPTION_ORDER_OUT;
	}

	public StringBuilder write(Graph<N, E> graph, Option... options) {
		return write(graph, null, null, new String[0], options);
	}

	public StringBuilder write(Graph<N, E> graph, List<Cluster<N>> clusters,
			CustomDottyMarkupCallback<N, E> markupCallback, String[] nodeExclusions, Option... options) {
		StringBuilder result = new StringBuilder();
		Map<N, Integer> nodeReferenceCount = GraphUtil.getInstance().getNodeReferenceCount(graph, null);

		writeHeader(graph, nodeReferenceCount, result, markupCallback, options);
		writeNodes(graph, nodeReferenceCount, result, markupCallback, nodeExclusions, options);
		writeEdges(graph, nodeReferenceCount, result, markupCallback, nodeExclusions, options);
		writeNodeClusters(graph, nodeReferenceCount, clusters, result, markupCallback, 1, nodeExclusions, options);
		writeFooter(graph, nodeReferenceCount, result, markupCallback, options);

		return result;
	}

	public StringBuilder write(Graph<N, E> graph, List<Cluster<N>> clusters,
			CustomDottyMarkupCallback<N, E> markupCallback, Option... options) {
		return write(graph, clusters, markupCallback, new String[0], options);
	}

	public StringBuilder write(Graph<N, E> graph, List<Cluster<N>> clusters) {
		return write(graph, clusters, null, new String[0]);
	}

	void writeHeader(Graph<N, E> graph, Map<N, Integer> nodeReferenceCount, StringBuilder result,
			CustomDottyMarkupCallback<N, E> markupCallback, Option... options) {
		result.append("digraph " + toValidNodeName(getName(graph)) + " {\n");
		if (EnumUtil.getInstance().contains(options, Option.GRAPH_OPTION_LEFT_TO_RIGHT))
			pad(result, 1).append("rankdir=LR;\n");
		else if (EnumUtil.getInstance().contains(options, Option.GRAPH_OPTION_TOP_TO_BOTTOM))
			pad(result, 1).append("rankdir=TB;\n");

		if (EnumUtil.getInstance().contains(options, Option.GRAPH_OPTION_ORDER_OUT))
			pad(result, 1).append("ordering=out;\n");

	}

	boolean excludeNode(N node, String[] nodeExclusions) {
		boolean result = false;
		for (String exclusion : nodeExclusions) {
			if (node.getClass().getSimpleName().matches(exclusion + "(Node)?")) {
				result = true;
				break;
			}
		}
		return result;
	}

	boolean excludeEdge(E edge, String[] nodeExclusions) {
		boolean result = false;
		for (String exclusion : nodeExclusions) {
			if (exclusion.startsWith("(Edge") && edge.getName().matches(exclusion.substring("(Edge)".length()))) {
				result = true;
				break;
			}
		}
		return result;
	}

	void writeNodes(Graph<N, E> graph, Map<N, Integer> nodeReferenceCount, StringBuilder result,
			CustomDottyMarkupCallback<N, E> markupCallback, String[] nodeExclusions, Option... options) {
		for (N node : graph.getNodes())
			writeNode(graph, node, nodeReferenceCount, result, markupCallback, nodeExclusions, options);
	}

	void writeNode(Graph<N, E> graph, N node, Map<N, Integer> nodeReferenceCount, StringBuilder result,
			CustomDottyMarkupCallback<N, E> markupCallback, String[] nodeExclusions, Option[] options) {
		boolean includeNode = !excludeNode(node, nodeExclusions);

		if (includeNode
				&& EnumUtil.getInstance().contains(options, Option.GRAPH_OPTION_CLUSTER_INCLUDE_RELATED_NODES_ONLY))
			includeNode = GraphUtil.getInstance().isNodeRelated(graph.getEdges(), node);
		if (includeNode) {
			String opmaak = "";
			if (null != markupCallback) {
				Integer refCount = nodeReferenceCount.get(node);
				opmaak = markupCallback.nodeMarkup(graph, node, (null != refCount) ? refCount : 0);
			}
			if (null != opmaak && !opmaak.isEmpty()) {
				if (opmaak.startsWith("[")) {
					// clean up empty groups
					opmaak = opmaak.replace("&apos", "'");
					opmaak = opmaak.replace("&quot", "\"");
				}
				opmaak = opmaak.replaceAll("\\|\"", "\"");
				opmaak = opmaak.replaceAll("\\|\"", "\"");
				opmaak = opmaak.replaceAll("\\|\\|", "|");
				opmaak = opmaak.replaceAll("\\|\\{[\\.\\|]?\\}", "");
				opmaak = opmaak.replaceAll("\\|\\}", "\\}");
				opmaak = opmaak.replaceAll("\\{\\|", "\\{");
				if (opmaak.startsWith("["))
					pad(result, 1).append(toValidNodeName(getName(node)) + " " + opmaak + ";\n");
				else
					pad(result, 1).append(toValidNodeName(getName(node)) + " [label=\""
							+ toValidDescription(getDescription(node)) + "\", " + opmaak + "];\n");

			} else
				pad(result, 1).append(toValidNodeName(getName(node)) + " [label=\""
						+ toValidDescription(getDescription(node)) + "\", shape=Mrecord];\n");
		}
	}

	void writeEdges(Graph<N, E> graph, Map<N, Integer> nodeReferenceCount, StringBuilder result,
			CustomDottyMarkupCallback<N, E> markupCallback, String[] nodeExclusion, Option... options) {
		for (E edge : graph.getEdges()) {
			if (!excludeNode(edge.getSource(), nodeExclusion) && !excludeNode(edge.getTarget(), nodeExclusion)
					&& !excludeEdge(edge, nodeExclusion)) {
				pad(result, 1).append(toValidNodeName(getName(edge.getSource())));
				result.append(" -> ");
				result.append(toValidNodeName(getName(edge.getTarget())));
				String edgeMarkup = null;
				if (null != markupCallback)
					edgeMarkup = markupCallback.edgeMarkup(graph, edge);
				if (null != edgeMarkup && !edgeMarkup.isEmpty())
					result.append(" [" + edgeMarkup + "]");
				else if (null != edge.getDescription())
					result.append(" [label=\"" + toValidDescription(getDescription(edge)) + "\"]");

				result.append(";\n");
			}
		}
	}

	void writeNodeClusters(Graph<N, E> graph, Map<N, Integer> nodeReferenceCount, List<Cluster<N>> clusters,
			StringBuilder result, CustomDottyMarkupCallback<N, E> markupCallback, int depth, String[] nodeExclusions,
			Option... options) {
		if (null != clusters) {
			for (Cluster<N> cluster : clusters) {
				pad(result, depth).append("subgraph \"cluster_" + cluster.getName() + "\" {\n");
				if (EnumUtil.getInstance().contains(options, Option.GRAPH_OPTION_ORDER_OUT)) {
					pad(result, depth).append("ordering=out;\n");
				}
				pad(result, depth + 1).append("label= \"" + toValidDescription(cluster.getDescription()) + "\"\n");
				if (null != markupCallback) {
					for (String markup : markupCallback.clusterMarkup(graph, cluster, depth)) {
						pad(result, depth + 1).append(markup + ";\n");
					}
				} else if (EnumUtil.getInstance().contains(options, Option.GRAPH_OPTION_GRAY)) {
					if ((((depth - 1) / 2) & 1) == 1) {
						pad(result, depth + 1).append("fillcolor=lightgrey;\n");
						pad(result, depth + 1).append("style=filled;\n");
					} else {
						pad(result, depth + 1).append("fillcolor=white;\n");
						pad(result, depth + 1).append("style=filled;\n");
					}
				}
				for (N node : cluster.getNodes())
					writeNode(graph, node, nodeReferenceCount, result, markupCallback, nodeExclusions, options);

				// Recursive call
				writeNodeClusters(graph, nodeReferenceCount, cluster.getSubClusters(), result, markupCallback,
						depth + 2, nodeExclusions, options);
				pad(result, depth).append("}\n");
			}
		}
	}

	void writeFooter(Graph<N, E> graph, Map<N, Integer> nodeReferenceCount, StringBuilder result,
			CustomDottyMarkupCallback<N, E> markupCallback, Option... options) {
		result.append("}\n");
	}

	public String toValidDescription(String description) {
		return unescapeQuotes(description);
	}

	public String toValidNodeName(String nodeName) {
		return "\"" + unescapeQuotes(nodeName) + "\"";
	}

	public String unescapeQuotes(String nodeName) {
		return nodeName.replace('\"', '\'');
	}

	public String getDescription(NameDescription nd) {
		String result;

		if (null != nd.getDescription())
			result = unescapeQuotes(nd.getDescription());
		else
			result = "" + nd.hashCode();

		return result;
	}

	public String getName(N node) {
		String result = node.getUniqueId();

		if (null == result)
			result = "" + node.hashCode();
		return result;
	}

	public String getName(E edge) {
		if (null == edge.getName())
			return edge.getClass().getName() + "_" + edge.hashCode();
		else
			return edge.getName();
	}

	public String getName(Graph<N, E> graph) {
		if (null == graph.getName())
			return graph.getClass().getName() + "_" + graph.hashCode();
		else
			return graph.getName();
	}

	public StringBuilder pad(StringBuilder result, int depth) {
		for (int i = 0; i < depth; ++i) {
			result.append("  ");
		}
		return result;
	}
}
