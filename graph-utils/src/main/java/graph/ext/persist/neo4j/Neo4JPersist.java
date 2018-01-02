package graph.ext.persist.neo4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import data.LogUtil;
import data.SequenceUtil;
import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.dm.Graph;
import graph.util.GraphUtil;
import persist.Connection;
import persist.DontCarePersistId;
import persist.Persist;
import persist.PersistId;

public class Neo4JPersist<N extends graph.dm.Node, E extends Edge<N>> implements Persist<Graph<N, E>, Integer> {
	public static final String ID = "id";
	public static final String UNIQUE_PROPERTY_NAME = "_unique_id";
	public static final String CREATION_TIMESTAMP_PROPERTY_NAME = "_gen_time";
	public static final String CREATION_SOURCE_CLASS_PROPERTY_NAME = "_gen_src_classname";
	public static final String CREATION_SOURCE_TYPE = "_gen_src_type";
	public static final String CREATION_USER_NAME = "_gen_user";
	public static final String CREATION_RUN_NUMBER_PROPERTY_NAME = "_gen_run_number";
	public static final String MODIFICATION_TIMESTAMP_PROPERTY_NAME = "_mod_time";
	public static final String MODIFICATION_USER_NAME = "_mod_user";
	public static final String MODIFICATION_RUN_NUMBER_PROPERTY_NAME = "_mod_run_number";
	public static final String NEO4J_PERSIST_RUN_NUMBER_SEQUENCE = "neo4j.persist.run.number";

	public enum SourceType {
		NODE, EDGE;
	}

	@Override
	public Graph<N, E> read(Connection conn, Class<Graph<N, E>> cls, PersistId<Integer> id, boolean createTransaction) {
		throw new UnsupportedOperationException(
				"can not read using [" + getClass().getName() + "], use REST connection");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public PersistId<Integer> save(Connection conn, Graph<N, E> graph, boolean createTransaction) {
		PersistId<Integer> result = new DontCarePersistId();
		Neo4JPersistContext ctx = new Neo4JPersistContext("system");
		Neo4JEmbeddedConnection n4jConn = (Neo4JEmbeddedConnection) conn;
		Transaction tx = null;
		int nodesAddedCount = 0;
		int relationsAddedCount = 0;
		try {
			if (createTransaction)
				tx = n4jConn.getDB().beginTx();

			List<E> edges = graph.getEdges();
			List<N> nodes = graph.getNodes();
			Map<N, Node> nodeMap = new HashMap<>();
			for (final N node : nodes) {
				Node n4jNode = null;
				Label lbl = new Label() {
					@Override
					public String name() {
						return GraphUtil.getInstance().makeLabelName(node.getLabel());
					}
				};
				n4jNode = n4jConn.getDB().findNode(lbl, UNIQUE_PROPERTY_NAME, node.getUniqueId());
				if (null == n4jNode) {
					n4jNode = n4jConn.getDB().createNode();
					n4jNode.addLabel(lbl);
					Map<String, Object> atts = GraphUtil.getInstance().object2NV(node, graph.dm.Node.class);

					for (Entry<String, Object> att : atts.entrySet()) {
						if (att.getValue() instanceof Enum)
							n4jNode.setProperty(att.getKey(), String.valueOf(att.getValue()));
						else if (att.getValue() instanceof List) {
							String value = "";
							for (Object el : (List) att.getValue())
								value += ((null != el) ? (el.toString() + " ") : "");
							if (value.endsWith(" "))
								value = value.substring(0, value.length() - 1);
							n4jNode.setProperty(att.getKey(), value);
						} else
							n4jNode.setProperty(att.getKey(), att.getValue());
						n4jNode.setProperty(UNIQUE_PROPERTY_NAME, node.getUniqueId());
					}
					enrich(ctx, node, n4jNode, true);
					nodeMap.put(node, n4jNode);
					++nodesAddedCount;
				} else {
					enrich(ctx, node, n4jNode, false);
					nodeMap.put(node, n4jNode);
				}
			}

			for (E edge : edges) {
				Node n4jNodeSource = nodeMap.get(edge.getSource());
				Node n4jNodeTarget = nodeMap.get(edge.getTarget());
				boolean relationExists = false;
				for (Relationship rel : n4jNodeSource.getRelationships(Direction.OUTGOING)) {
					if (rel.getEndNode().getId() == n4jNodeTarget.getId()
							&& rel.getProperty(UNIQUE_PROPERTY_NAME).equals(edge.getName())) {
						relationExists = true;
						enrich(ctx, edge, rel, false);
						break;
					}
				}
				if (!relationExists) {
					PersistRelationType<N, E> relType = new PersistRelationType<N, E>(edge);
					Relationship rel = n4jNodeSource.createRelationshipTo(n4jNodeTarget, relType);
					Map<String, Object> atts = GraphUtil.getInstance().object2NV(edge, graph.dm.Edge.class);
					rel.setProperty(UNIQUE_PROPERTY_NAME, edge.getName());
					for (Entry<String, Object> att : atts.entrySet()) {
						if (att.getValue() instanceof Enum)
							rel.setProperty(att.getKey(), String.valueOf(att.getValue()));
						else
							rel.setProperty(att.getKey(), att.getValue());
					}
					enrich(ctx, edge, rel, true);
					++relationsAddedCount;
				} else {
					relationExists = false; /* dummy statement */
				}

			}
			LogUtil.getInstance().info("added [" + nodesAddedCount + "] and [" + relationsAddedCount + "] to ["
					+ n4jConn.getStore() + "]");
		} catch (Exception e) {
			LogUtil.getInstance().log(getClass().getName(), Level.WARNING, "problem storing graph", e);
			if (null != tx) {
				tx.failure();
				tx.close();
			}
		} finally {
			if (null != tx) {
				tx.success();
				tx.close();
			}
		}
		return result;
	}

	private void enrich(Neo4JPersistContext ctx, Object source, PropertyContainer props, boolean isNew) {
		if (isNew) {
			props.setProperty(CREATION_TIMESTAMP_PROPERTY_NAME, ctx.getTimestamp());
			props.setProperty(CREATION_USER_NAME, ctx.getUser());
			props.setProperty(CREATION_RUN_NUMBER_PROPERTY_NAME, ctx.getRunNumber());
			props.setProperty(CREATION_SOURCE_CLASS_PROPERTY_NAME, source.getClass().getName());
			props.setProperty(CREATION_SOURCE_TYPE, (graph.dm.Node.class.isAssignableFrom(source.getClass()))
					? SourceType.NODE.name() : SourceType.EDGE.name());
		}
		props.setProperty(MODIFICATION_TIMESTAMP_PROPERTY_NAME, ctx.getTimestamp());
		props.setProperty(MODIFICATION_USER_NAME, ctx.getUser());
		props.setProperty(MODIFICATION_RUN_NUMBER_PROPERTY_NAME, ctx.getRunNumber());
	}

	private static class PersistRelationType<N extends graph.dm.Node, E extends Edge<N>> implements RelationshipType {
		private E edge;

		public PersistRelationType(E edge) {
			this.edge = edge;
		}

		@Override
		public String name() {
			return (null != edge.getName()) ? edge.getName() : EdgeType.HAS;
		}
	}

	public class Neo4JPersistContext {
		private String user;
		private String timestamp;
		private String runNumber;

		public Neo4JPersistContext(String user) {
			Date time = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd kk:mm");
			this.timestamp = fmt.format(time);
			this.runNumber = SequenceUtil.getInstance().getNext(NEO4J_PERSIST_RUN_NUMBER_SEQUENCE);
			this.user = user;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public String getRunNumber() {
			return runNumber;
		}

		public void setRunNumber(String runNumber) {
			this.runNumber = runNumber;
		}
	}
}
