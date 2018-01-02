package graph.ext.persist.neo4j;

import java.util.List;

import csv.CSVData;
import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import graph.io.GraphMetrics;
import json.JSONList;
import json.JSONObject;
import json.JSONRecord;
import json.JSONUtil;
import json.JSONValue;
import rest.RESTClient;

public class Neo4JRESTConnection {
	public enum CypherResultType {
		GRAPH("graph"), CSV("row");
		private String neo4jType;

		private CypherResultType(String neo4jType) {
			this.neo4jType = neo4jType;
		}

		public String getNeo4JType() {
			return neo4jType;
		}
	}

	private static final String PASSWORD_ENDPOINT = "/user/neo4j/password";
	private static final String CYPHER_ENDPOINT = "/db/data/transaction/commit";
	private String endpoint;
	private String user;
	private String password;

	public Neo4JRESTConnection(String endpoint, String user, String password) {
		this.endpoint = endpoint;
		this.user = user;
		this.password = password;
	}

	public JSONObject setInitialPassword() {
		RESTClient cln = new RESTClient();
		LogUtil.getInstance().info("neo4j - set initial password, assuming neo4j/neo4j");
		cln.setHeaders(new String[][] { { RESTClient.ACCEPT, "application/json; charset=UTF-8" },
				{ RESTClient.CONTENT_TYPE, "application/json" } });
		cln.setAuthorization("neo4j", "neo4j");
		JSONRecord req = new JSONRecord();
		req.getData().put("password", new JSONValue<String>(password));
		String json = cln.post(getEndpoint() + PASSWORD_ENDPOINT, new String[][] {}, req, e -> cln.entity2String(e));
		JSONObject result = null;
		if (null != json && !json.isEmpty()) {
			result = JSONUtil.getInstance().parseJSON(json);
			logNeo4JErrors(result);
		}

		return result;
	}

	private void logNeo4JErrors(JSONObject result) {
		if (null != result) {
			List<String[]> errors = Neo4JUtil.getInstance().getNeo4JErrors(result);

			if (!errors.isEmpty())
				for (String[] error : errors)
					LogUtil.getInstance().error("neo4j error [" + error[0] + ":" + error[1] + "]");
			else
				LogUtil.getInstance().info("OK");
		}
	}

	public JSONObject cypher(String cypher) {
		return cypher(cypher, CypherResultType.GRAPH);
	}

	public JSONObject cypher(String cypher, CypherResultType resultType) {
		RESTClient cln = new RESTClient();
		LogUtil.getInstance().info("neo4j - executing [" + cypher + "]");
		cln.setHeaders(new String[][] { { RESTClient.ACCEPT, "application/json; charset=UTF-8" },
				{ RESTClient.CONTENT_TYPE, "application/json" } });
		cln.setAuthorization(user, password);
		JSONList statements = new JSONList();
		JSONRecord statement = new JSONRecord();
		statement.getData().put("statement", new JSONValue<String>(cypher));
		statements.getData().add(statement);
		JSONList resultContents = new JSONList();
		resultContents.getData().add(new JSONValue<String>(resultType.getNeo4JType()));
		statement.getData().put("resultDataContents", resultContents);
		JSONRecord req = new JSONRecord();
		req.getData().put("statements", statements);
		// InputStream json = cln.post(getEndpoint() + CYPHER_ENDPOINT, new
		// String[][] {}, req);
		String json = cln.post(getEndpoint() + CYPHER_ENDPOINT, new String[][] {}, req, e -> cln.entity2String(e));
		DataUtil.getInstance()
				.writeToFile(ConfigurationUtil.getInstance().getTmpDir() + DataUtil.PATH_SEPARATOR + "resp.json", json);
		LogUtil.getInstance().info("neo4j - parse result");
		JSONObject result = JSONUtil.getInstance().parseJSON(json);
		logNeo4JErrors(result);

		return result;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public static void main(String[] args) {
		Neo4JRESTConnection conn = new Neo4JRESTConnection("http://localhost:7474/db/data", "neo4j", "1");
		JSONObject json = conn.cypher("", CypherResultType.GRAPH);
		Graph<Node, Edge<Node>> gra = Neo4JUtil.getInstance().convertNeo4JResult2Graph(json);
		gra.setName("neo4jresttest.1");
		GraphMetrics metrics = new GraphMetrics();
		metrics.writeGraphWithCycleInfo("d:/tmp/", gra, new CSVData());
		json = conn.cypher("match p1 = (p:ProxyService) -[*]-> (b:BusinessService)  return p1", CypherResultType.GRAPH);
		gra = Neo4JUtil.getInstance().convertNeo4JResult2Graph(json);
		gra.setName("neo4jresttest.2");
		metrics.writeGraphWithCycleInfo("d:/tmp/", gra, new CSVData());
		return;
	}
}
