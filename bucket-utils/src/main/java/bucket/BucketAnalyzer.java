package bucket;

import java.util.List;

import graph.GraphOption;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import stat.CounterManager;

public class BucketAnalyzer {
	public static final String BUCKET_TYPE_GRAPH_NAME = "bucket_type";

	public Graph<Node, Edge<Node>> getBucketMetaGraph(BucketManager bucketManager, Graph<Node, Edge<Node>> gra) {
		final Graph<Node, Edge<Node>> result = (null == gra) ? new Graph<>() : gra;

		bucketManager.stream().forEach(b -> b.stream().forEach(t -> {
			List<Edge<Node>> edges = result
					.filterEdges(e -> e.getSource().getId().equals(t.getKey().getClass().getName())
							&& e.getTarget().getId().equals(t.getContent().getClass().getName())
							&& e.getName().equals(t.getType()));
			if (edges.isEmpty()) {
				Node source = new Node();
				source.setId(t.getKey().getClass().getName());
				source.setName(t.getKey().getClass().getSimpleName());
				source.setDescription(source.getName());
				Node target = new Node();
				target.setId(t.getContent().getClass().getName());
				target.setName(t.getContent().getClass().getSimpleName());
				target.setDescription(target.getName());
				Edge<Node> edge = new TypeEdge(source, target, t.getType());

				result.addEdge(edge, GraphOption.CHECK_DUPLICATES);
			}
		}));
		result.setName(BUCKET_TYPE_GRAPH_NAME);
		return result;
	}

	public CounterManager getBucketStats(BucketManager bucketManager, final CounterManager counters) {
		bucketManager.stream().forEach(b -> counters.incrementOk(b.getName(), b.size()));

		return counters;
	}

	public class TypeEdge extends Edge<Node> {
		public TypeEdge(Node source, Node target, String name) {
			super(source, target, name, name);
		}
	}
}
