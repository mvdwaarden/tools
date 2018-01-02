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
	public <N extends Node, E extends Edge<N>> boolean writeGraphWithCycleInfo(String targetdir, Graph<N, E> gra,
			CSVData nodemarkup, GraphOption... options) {
		return writeGraphWithCycleInfo(targetdir, gra, null, nodemarkup, options);
	}

	/**
	 * Write graph cycle info
	 */

	public <N extends Node, E extends Edge<N>> boolean writeGraphWithCycleInfo(String targetdir, Graph<N, E> gra,
			List<Cluster<N>> clusters, CSVData nodemarkup, GraphOption... options) {
		return writeGraphWithCycleInfo(targetdir, gra, clusters, nodemarkup, new String[0], options);
	}

	/**
	 * Write graph cycle info
	 */
	public <N extends Node, E extends Edge<N>> boolean writeGraphWithCycleInfo(String targetdir, Graph<N, E> gra,
			List<Cluster<N>> clusters, CSVData nodemarkup, String[] nodeExclusions, GraphOption... options) {

		boolean result = false;

		SimpleDottyWriter<N, E> wri = new SimpleDottyWriter<N, E>();
		class Locals {
			GraphCycleChecker<N, E> cc;
			List<List<E>> cycles;
			List<List<E>> uncleanCycles;
		}
		;
		final Locals _locals = new Locals();

		if (EnumUtil.getInstance().contains(options, GraphOption.WRITE_CYCLE_INFO)) {
			_locals.cc = new GraphCycleChecker<N, E>();
			_locals.cycles = _locals.cc.checkCycles(gra, true);
			if (EnumUtil.getInstance().contains(options, GraphOption.CLEANUP_CYCLE_INFO)) {
				_locals.cc.cleanup(_locals.cycles);
				_locals.uncleanCycles = _locals.cc.checkCycles(gra, true);
			} else {
				_locals.uncleanCycles = _locals.cycles;
			}
			if (EnumUtil.getInstance().contains(options, GraphOption.DUMP_CYCLE_INFO)) {
				if (!_locals.cycles.isEmpty()) {
					result = true;
					for (List<E> cycle : _locals.cycles) {
						String line = " > " + cycle.get(0).getSource().getId() + " -> ";
						for (E edge : cycle) {
							line += edge.getTarget().getId() + " -> ";
						}
						line += ":";
						System.out.println("cycle " + line);
					}
				}
			}
			DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + gra.getName() + ".gv",
					wri.write(gra, clusters, (null == clusters) ? new CustomDottyMarkupCallback<N, E>() {
						@Override
						public String nodeMarkup(Graph<N, E> graph, N node, int referenceCount) {
							String result = null;
							if (_locals.cc.isNodeContainedInCycles(_locals.cycles, node)) {
								result = "shape=hexagon, color=red";
							}
							return result;
						}

						@Override
						public String edgeMarkup(Graph<N, E> gra, E edge) {
							String result = null;
							if (_locals.cc.isEdgeContainedInCycles(_locals.uncleanCycles, edge)) {
								result = "color=red";
							}
							return result;
						}

						@Override
						public String[] clusterMarkup(Graph<N, E> graph, Cluster<N> cluster, int depth) {
							return null;
						}
					} : null, new String[0], SimpleDottyWriter.Option.GRAPH_OPTION_LEFT_TO_RIGHT).toString());

		} else {
			GraphIndex<N, E> gidx = new GraphIndex<N, E>(gra).build();
			DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + gra.getName() + ".gv",
					wri.write(gra, clusters, new CustomDottyMarkupCallback<N, E>() {
						@Override
						public String nodeMarkup(Graph<N, E> graph, N node, int referenceCount) {
							String result;
							String markup = nodemarkup.get(GraphConst.CONFIG_COLUMN_CLASS, node.getId(),
									GraphConst.CONFIG_COLUMN_MARKUP);
							/* TODO callback is parameter! */
							if (null == markup || markup.isEmpty())
								markup = nodemarkup.get(GraphConst.CONFIG_COLUMN_CLASS, node.getClass().getName(),
										GraphConst.CONFIG_COLUMN_MARKUP);
							if (null != markup)
								result = StringUtil.getInstance().replace(markup, var -> {
									GraphQuery<N, E> qry = new GraphQuery<>(gidx, node);
									Object value = qry.getByPath(var, '|');
									return (null == value) ? "" : value.toString().replaceAll("\"", "\\\"");
								});
							else
								result = markup;
							if (null != result) {
								// clean up empty groups
								result = result.replaceAll("\\|\\|", "|");
								result = result.replaceAll("\\|\\{[\\.\\|]?\\}", "");
								result = result.replaceAll("\\|\\}", "\\}");
								result = result.replaceAll("\\{\\|", "\\{");
							}
							return result;
						}

						@Override
						public String[] clusterMarkup(Graph<N, E> graph, Cluster<N> cluster, int depth) {
							return new String[] {};
						}

						@Override
						public String edgeMarkup(Graph<N, E> gra, E edge) {
							String key = GraphConst.CONFIG_COLUMN_CLASS_SIMPLE_EDGE + "." + edge.getName();
							String markup = nodemarkup.get(GraphConst.CONFIG_COLUMN_CLASS, key,
									GraphConst.CONFIG_COLUMN_MARKUP);

							return markup;
						}
					}, nodeExclusions, SimpleDottyWriter.Option.GRAPH_OPTION_LEFT_TO_RIGHT).toString());
		}
		return result;
	}
}
