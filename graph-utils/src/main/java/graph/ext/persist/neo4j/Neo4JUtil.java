package graph.ext.persist.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import csv.CSVData;
import data.EnumUtil;
import data.LogUtil;
import data.Util;
import graph.GraphOption;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import json.JSONList;
import json.JSONObject;
import json.JSONQuery;
import json.JSONRecord;
import json.JSONValue;
import object.ObjectFactory;
import object.ObjectIterator;
import object.ObjectUtil;

public class Neo4JUtil implements Util {
	private static final ThreadLocal<Neo4JUtil> instance = new ThreadLocal<Neo4JUtil>();
	private static final String EDGE_NAME_SYNTHETIC = "syn";

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static Neo4JUtil getInstance() {
		Neo4JUtil result = instance.get();

		if (null == result) {
			result = new Neo4JUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * Clone the thread local to another thread, typically called before the
	 * thread is actually run.
	 * 
	 * @param util
	 */
	public void clone(Neo4JUtil instance) {

		Neo4JUtil.instance.set(instance);
	}

	public <N extends Node, E extends Edge<N>> Graph<N, E> convertNeo4JResult2Graph(JSONObject neo4jResult) {
		Graph<N, E> result = new Graph<>();
		JSONQuery qry = new JSONQuery(neo4jResult);
		Map<Integer, N> cacheNodes = new HashMap<>();

		for (JSONObject jsonData : qry.get("results").get(0).get("data").<JSONList> get().getData()) {
			JSONQuery row = new JSONQuery(jsonData).get("row");
			if (!row.isEmpty())
				addRow(result, row);
			JSONQuery graph = new JSONQuery(jsonData).get("graph");
			if (!graph.isEmpty())
				addGraph(result, graph, cacheNodes);
		}

		return result;
	}

	public CSVData convertNeo4JResult2CSV(JSONObject neo4jResult) {
		CSVData result = new CSVData();
		JSONQuery qry = new JSONQuery(neo4jResult);
		JSONList columns = qry.get("results").get(0).get("columns").<JSONList> get();
		String[] header = new String[columns.getData().size()];
		int headerIdx = 0;
		for (JSONObject jsonData : columns.getData()) {
			@SuppressWarnings("unchecked")
			JSONValue<String> col = (JSONValue<String>) jsonData;
			header[headerIdx++] = col.getStringValue();
		}
		result.setHeader(header);
		for (JSONObject jsonData : qry.get("results").get(0).get("data").<JSONList> get().getData()) {
			String[] row = new String[header.length];
			JSONList jsonRow = new JSONQuery(jsonData).get("row").<JSONList> get();
			int cellIdx = 0;
			for (JSONObject cell : jsonRow.getData()) {
				@SuppressWarnings("unchecked")
				JSONValue<String> value = (JSONValue<String>) cell;
				row[cellIdx++] = value.getStringValue();				
			}
			result.add(row);
		}
		return result;
	}

	public List<String[]> getNeo4JErrors(JSONObject neo4jResult) {
		JSONQuery qry = new JSONQuery(neo4jResult);
		List<String[]> result = new ArrayList<>();

		for (JSONObject jsonData : qry.get("errors").<JSONList> get().getData()) {
			JSONQuery row = new JSONQuery(jsonData);
			String[] nfoError = new String[2];
			nfoError[0] = row.get("message").<JSONValue<String>> get().getStringValue();
			nfoError[1] = row.get("code").<JSONValue<String>> get().getStringValue();
			result.add(nfoError);
		}

		return result;
	}

	protected <N extends Node, E extends Edge<N>> void addGraph(Graph<N, E> gra, JSONQuery graph,
			Map<Integer, N> cacheNodes) {
		JSONQuery nodes = graph.get("nodes");
		for (int i = 0; i < nodes.length(); ++i) {
			JSONQuery qryNode = nodes.get(i);
			Integer id = Integer.parseInt(qryNode.get("id").<JSONValue<String>> get().getStringValue());
			N node = cacheNodes.get(id);
			if (null == node) {
				JSONRecord props = qryNode.get("properties").<JSONRecord> get();
				@SuppressWarnings("unchecked")
				final String uid = ((JSONValue<String>) props.getData().get(Neo4JPersist.ID)).getStringValue();
				node = createNode(uid, props);
				cacheNodes.put(id, node);
				gra.addNode(node);
			}
		}

		JSONQuery edges = graph.get("relationships");

		for (int i = 0; i < edges.length(); ++i) {
			JSONQuery qryNode = edges.get(i);
			Integer startId = Integer.parseInt(qryNode.get("startNode").<JSONValue<String>> get().getStringValue());
			Integer endId = Integer.parseInt(qryNode.get("endNode").<JSONValue<String>> get().getStringValue());
			N source = cacheNodes.get(startId);
			N target = cacheNodes.get(endId);
			JSONRecord props = qryNode.get("properties").<JSONRecord> get();

			E edge = createEdge(props);
			edge.setSource(source);
			edge.setTarget(target);
			gra.addEdge(edge, GraphOption.CHECK_DUPLICATES);
		}
	}

	protected <N extends Node, E extends Edge<N>> void addRow(Graph<N, E> gra, JSONQuery row) {
		Stack<JSONRecord> tripple = new Stack<>();
		for (int i = 0; i < row.length(); ++i) {
			JSONObject jsonRow = row.get(i).get();
			if (jsonRow instanceof JSONList) {
				addRow(gra, row.get(i));
			} else {
				JSONRecord rec = row.get(i).<JSONRecord> get();
				Neo4JPersist.SourceType curType = null;
				Neo4JPersist.SourceType type = getType(rec);
				if (tripple.isEmpty() && null != type) {
					tripple.push(rec);
				} else if (null != type) {
					switch (type) {
					case NODE:
						curType = getType(tripple.peek());
						if (curType == Neo4JPersist.SourceType.NODE) {
							// add synthetic edge
							JSONRecord edge = new JSONRecord();
							edge.getData().put(Neo4JPersist.UNIQUE_PROPERTY_NAME,
									new JSONValue<String>(EDGE_NAME_SYNTHETIC));
							edge.getData().put("description", new JSONValue<String>(EDGE_NAME_SYNTHETIC));
							edge.getData().put(Neo4JPersist.CREATION_SOURCE_CLASS_PROPERTY_NAME,
									new JSONValue<String>(Edge.class.getName()));
							tripple.push(edge);
							tripple.push(rec);
						} else
							tripple.push(rec);
						break;
					case EDGE:
						tripple.push(rec);
						break;
					}
				}
				if (tripple.size() == 3) {
					JSONRecord from = tripple.get(0);
					JSONRecord edge = tripple.get(1);
					JSONRecord to = tripple.get(2);

					if (null != from && null != to && null != edge)
						addEdge(gra, from, to, edge);
					tripple.pop();
					tripple.pop();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <N extends Node, E extends Edge<N>> void addEdge(Graph<N, E> gra, JSONRecord from, JSONRecord to,
			JSONRecord edge) {
		final String sourceId = ((JSONValue<String>) from.getData().get(Neo4JPersist.UNIQUE_PROPERTY_NAME))
				.getStringValue();
		List<N> nodes = gra.filterNodes(n -> n.getId().equals(sourceId));
		N sourceNode = null;
		if (nodes.isEmpty()) {
			sourceNode = createNode(sourceId, from);
		} else
			sourceNode = nodes.get(0);
		final String targetId = ((JSONValue<String>) to.getData().get(Neo4JPersist.UNIQUE_PROPERTY_NAME))
				.getStringValue();
		nodes = gra.filterNodes(n -> n.getId().equals(targetId));
		N targetNode = null;
		if (nodes.isEmpty()) {
			targetNode = createNode(targetId, to);
		} else
			targetNode = nodes.get(0);

		E graEdge = createEdge(edge);
		graEdge.setSource(sourceNode);
		graEdge.setTarget(targetNode);
		gra.addEdge(graEdge, GraphOption.CHECK_DUPLICATES);
	}

	@SuppressWarnings("unchecked")
	public Object createObject(JSONRecord json, Class<?> baseClass) {
		Object result = null;
		JSONValue<String> value = (JSONValue<String>) json.getData()
				.get(Neo4JPersist.CREATION_SOURCE_CLASS_PROPERTY_NAME);

		if (null != value) {
			String cls = value.getStringValue();
			try {
				result = ObjectFactory.getInstance().createObject(cls);
				Map<String, Field> fields = ObjectIterator.lookupFields(baseClass, result.getClass());
				for (Entry<String, JSONObject> e : json.getData().entrySet()) {
					if (e.getValue() instanceof JSONValue) {
						Field f = fields.get(e.getKey());
						if (null != f) {
							ObjectUtil.getInstance().setValue(result, f,
									((JSONValue<?>) e.getValue()).getStringValue());
						}
					}

				}
			} catch (Exception e) {
				LogUtil.getInstance().ignore("unable to create a [" + cls + "], check if ["
						+ Neo4JPersist.CREATION_SOURCE_CLASS_PROPERTY_NAME + "] properties are correct", e);
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <N extends Node, E extends Edge<N>> E createEdge(JSONRecord edge) {
		Object obj = createObject(edge, Edge.class);
		E result;
		if (obj instanceof Edge) {
			result = (E) obj;
		} else {
			result = (E) new Edge();
			JSONObject json = edge.getData().get(Neo4JPersist.UNIQUE_PROPERTY_NAME);			
			String name = (null != json) ? ((JSONValue<String>)json).getStringValue() : "null";
			result.setName(name);
			if (null != json)
				result.setDescription(name);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	protected <N extends Node> N createNode(String id, JSONRecord node) {
		Object obj = createObject(node, Node.class);
		N result;

		if (obj instanceof Node) {
			result = (N) obj;
		} else {
			result = (N) new Node();
			String name = ((JSONValue<String>) node.getData().get("name")).getStringValue();
			String description = ((JSONValue<String>) node.getData().get("description")).getStringValue();
			result.setName(name);
			result.setDescription(description);
		}

		result.setId(id);

		return result;
	}

	@SuppressWarnings("unchecked")
	protected Neo4JPersist.SourceType getType(JSONRecord json) {
		return (Neo4JPersist.SourceType) EnumUtil.getInstance().getByName(Neo4JPersist.SourceType.class,
				((JSONValue<String>) json.getData().get(Neo4JPersist.CREATION_SOURCE_TYPE)).getStringValue());
	}
}
