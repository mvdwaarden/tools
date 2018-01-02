package graph.dm;

import java.util.ArrayList;
import java.util.List;

import data.EnumUtil;
import data.LogUtil;
import graph.GraphOption;

public class Cluster<N extends Node> {
	String name;
	String description;
	List<N> nodes;
	List<Cluster<N>> subClusters;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<N> getNodes() {
		if (null == nodes) {
			nodes = new ArrayList<>();
		}
		return nodes;
	}

	public void addNode(N node) {
		getNodes().add(node);
	}

	public <E extends Edge<N>> void addNodes(Graph<N, E> gra) {
		for (N node : gra.getNodes())
			addNode(node);
	}

	public List<Cluster<N>> getSubClusters() {
		if (null == subClusters) {
			subClusters = new ArrayList<>();
		}
		return subClusters;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public boolean contains(N node, GraphOption... options) {
		return null != findCluster(node, options);
	}

	public N getRoot() {
		N result = null;
		if (!getNodes().isEmpty())
			result = getNodes().get(0);

		return result;
	}

	public void setRoot(N root) {
		if (getNodes().isEmpty())
			getNodes().add(root);
		else
			LogUtil.getInstance().warning("root already defined");
	}

	public Cluster<N> findCluster(N node, GraphOption... options) {
		Cluster<N> result = null;
		boolean contains = false;

		for (N n : nodes) {
			if (n.treatAsSame(node)) {
				contains = true;
				break;
			}
		}
		if (contains)
			result = this;
		if (null != subClusters && EnumUtil.getInstance().contains(options, GraphOption.CLUSTER_RECURSIVE)) {
			for (Cluster<N> cluster : subClusters) {
				result = cluster.findCluster(node, options);
				if (null != result)
					break;
			}
		}

		return result;
	}
}
