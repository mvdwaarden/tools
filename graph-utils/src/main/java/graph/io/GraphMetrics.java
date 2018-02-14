package graph.io;

import java.util.List;

import csv.CSVData;
import data.DataUtil;
import data.EnumUtil;
import data.StringUtil;
import graph.GraphConst;
import graph.GraphOption;
import graph.dm.Cluster;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.GraphIndex;
import graph.dm.GraphQuery;
import graph.dm.Node;
import graph.util.GraphCycleChecker;

public class GraphMetrics {
	/**
	 * Write graph cycle info
	 * 
	 * @param targetdir
	 * @param gra
	 * @param cleanup
	 */
	public <N extends Node, E extends Edge<N>> boolean writeGrapInfo(String targetdir, Graph<N, E> gra,
			CSVData nodemarkup, GraphOption... options) {
		return writeGraphInfo(targetdir, gra, null, nodemarkup, options);
	}

	/**
	 * Write graph cycle info
	 */

	public <N extends Node, E extends Edge<N>> boolean writeGraphInfo(String targetdir, Graph<N, E> gra,
			List<Cluster<N>> clusters, CSVData nodemarkup, GraphOption... options) {
		return writeGraphInfo(targetdir, gra, clusters, nodemarkup, new String[0], options);
	}

	/**
	 * Write graph cycle info
	 */
	public <N extends Node, E extends Edge<N>> boolean writeGraphInfo(String targetdir, Graph<N, E> gra,
			List<Cluster<N>> clusters, CSVData nodemarkup, String[] nodeExclusions, GraphOption... options) {

		boolean result = false;

		SimpleDottyWriter<N, E> wri = new SimpleDottyWriter<N, E>();

		if (EnumUtil.getInstance().contains(options, GraphOption.WRITE_CYCLE_INFO)) {
			GraphCycleChecker<N, E> cc = new GraphCycleChecker<N, E>();
			List<List<E>> cycles = cc.checkCycles(gra, true);
			List<List<E>> uncleanCycles;

			if (EnumUtil.getInstance().contains(options, GraphOption.CLEANUP_CYCLE_INFO)) {
				cc.cleanup(cycles);
				uncleanCycles = cc.checkCycles(gra, true);
			} else {
				uncleanCycles = cycles;
			}
			if (EnumUtil.getInstance().contains(options, GraphOption.DUMP_CYCLE_INFO)) {
				if (!cycles.isEmpty()) {
					result = true;
					for (List<E> cycle : cycles) {
						String line = " > " + cycle.get(0).getSource().getId() + " -> ";
						for (E edge : cycle) {
							line += edge.getTarget().getId() + " -> ";
						}
						line += ":";
						System.out.println("cycle " + line);
					}
				}
			}
			DataUtil.getInstance()
					.writeToFile(
							targetdir + DataUtil.PATH_SEPARATOR + gra.getName() + "_cycles.gv", wri
									.write(gra, clusters, new GraphCycleMarkupCallback<N, E>(cc, cycles, uncleanCycles),
											new String[0], SimpleDottyWriter.Option.GRAPH_OPTION_LEFT_TO_RIGHT)
									.toString());

		}
		GraphIndex<N, E> gidx = new GraphIndex<N, E>(gra).build();
		DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + gra.getName() + ".gv",
				wri.write(gra, null, new NodeConfigMarkupCallback<N, E>(gidx, nodemarkup), nodeExclusions,
						SimpleDottyWriter.Option.GRAPH_OPTION_LEFT_TO_RIGHT).toString());
		if (null != clusters && !clusters.isEmpty()) {
			DataUtil.getInstance()
					.writeToFile(
							targetdir + DataUtil.PATH_SEPARATOR + gra.getName() + "_clustered.gv", wri
									.write(gra, clusters, new NodeConfigMarkupCallback<N, E>(gidx, nodemarkup),
											nodeExclusions, SimpleDottyWriter.Option.GRAPH_OPTION_LEFT_TO_RIGHT)
									.toString());
		}

		return result;
	}

	public static class GraphCycleMarkupCallback<N extends Node, E extends Edge<N>>
			implements CustomDottyMarkupCallback<N, E> {
		private GraphCycleChecker<N, E> cc;
		private List<List<E>> cycles;
		private List<List<E>> uncleanCycles;

		public GraphCycleMarkupCallback(GraphCycleChecker<N, E> cc, List<List<E>> cycles, List<List<E>> uncleanCycle) {
			this.cc = cc;
			this.cycles = cycles;
			this.uncleanCycles = uncleanCycle;
		}

		@Override
		public String nodeMarkup(Graph<N, E> graph, N node, int referenceCount) {
			String result = null;
			if (cc.isNodeContainedInCycles(cycles, node)) {
				result = "shape=hexagon, color=red";
			}
			return result;
		}

		@Override
		public String edgeMarkup(Graph<N, E> gra, E edge) {
			String result = null;
			if (cc.isEdgeContainedInCycles(uncleanCycles, edge)) {
				result = "color=red";
			}
			return result;
		}

		@Override
		public String[] clusterMarkup(Graph<N, E> graph, Cluster<N> cluster, int depth) {
			return new String[0];
		}
	};

	public static class NodeConfigMarkupCallback<N extends Node, E extends Edge<N>>
			implements CustomDottyMarkupCallback<N, E> {
		private GraphIndex<N, E> gidx;
		private CSVData nodemarkup;

		public NodeConfigMarkupCallback(GraphIndex<N, E> gidx, CSVData nodemarkup) {
			this.gidx = gidx;
			this.nodemarkup = nodemarkup;
		}

		public String nodeMarkup(Graph<N, E> graph, N node, int referenceCount) {
			String result;
			String markup = nodemarkup.get(GraphConst.CONFIG_COLUMN_CLASS, node.getId(),
					GraphConst.CONFIG_COLUMN_MARKUP);
			if (null == markup || markup.isEmpty())
				markup = nodemarkup.get(GraphConst.CONFIG_COLUMN_CLASS, node.getClass().getName(),
						GraphConst.CONFIG_COLUMN_MARKUP);
			if (null != markup)
				result = StringUtil.getInstance().replace(markup, var -> {
					GraphQuery<N, E> qry = new GraphQuery<>(gidx, node);
					Object value = qry.getByPath(var, '|');
					return (null == value) ? "" : value.toString().replaceAll("\"", "");
				});
			else
				result = markup;
			
			return result;
		}

		@Override
		public String[] clusterMarkup(Graph<N, E> graph, Cluster<N> cluster, int depth) {
			return new String[] {};
		}

		@Override
		public String edgeMarkup(Graph<N, E> gra, E edge) {
			String key = GraphConst.CONFIG_COLUMN_CLASS_SIMPLE_EDGE + "." + edge.getName();
			String markup = nodemarkup.get(GraphConst.CONFIG_COLUMN_CLASS, key, GraphConst.CONFIG_COLUMN_MARKUP);

			return markup;
		}
	};
}
