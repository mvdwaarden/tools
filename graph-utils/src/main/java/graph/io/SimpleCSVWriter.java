package graph.io;

import java.util.List;

import csv.CSVData;
import graph.dm.Cluster;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;

public class SimpleCSVWriter <N extends Node,E extends Edge<N>> {
	public CSVData write(Graph<N,E> graph, List<Cluster<N>> clusters, int minLevel, int options) {
		CSVData result = new CSVData();

		result.add(new String[] { "VAN", "VAN_BESCHRIJVING", "VAN_CLUSTER", "NAAR", "NAAR_BESCHRIJVING",
				"NAAR_CLUSTER", "RELATIE", "RELATIE_BESCHRIJVING" });
		for (Edge<N> edge : graph.getEdges()) {
			String[] line = new String[8];
			int i = 0;
			line[i++] = edge.getSource().getId();
			line[i++] = edge.getSource().getDescription();
			Cluster<N> clu = getCluster(edge.getSource(), clusters);
			if (null != clu)
				line[i++] = clu.getName();
			else
				i++;
			line[i++] = edge.getTarget().getId();
			line[i++] = edge.getTarget().getDescription();
			clu = getCluster(edge.getTarget(), clusters);
			if (null != clu)
				line[i++] = clu.getName();
			else
				i++;
			line[i++] = edge.getName();
			line[i++] = edge.getDescription();
			result.add(line);
		}

		return result;
	}

	private Cluster<N> getCluster(Node node, List<Cluster<N>> clusters) {
		Cluster<N> result = null;

		if (null != clusters) {
			findCluster: for (Cluster<N> cluster : clusters) {
				for (Node cnode : cluster.getNodes()) {
					if (cnode.equals(node)) {
						result = cluster;
						break findCluster;
					}
				}
				result = getCluster(node, cluster.getSubClusters());
				if (null != result) {
					break findCluster;
				}
			}
		}

		return result;
	}
}
