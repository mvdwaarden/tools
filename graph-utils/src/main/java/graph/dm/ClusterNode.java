package graph.dm;

public class ClusterNode<N extends Node> extends Node {
	Cluster<N> cluster;

	public Cluster<N> getCluster() {
		return cluster;
	}

	public void setCluster(Cluster<N> cluster) {
		this.cluster = cluster;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean treatAsSame(Node node) {
		boolean result = false;

		if (node instanceof ClusterNode)
			result = ((ClusterNode<N>) node).getCluster().equals(cluster);

		return result;
	}
}
