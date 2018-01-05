package nl.ordina.tools.soatool.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import csv.CSVData;
import csv.CSVUtil;
import data.ConfigurationUtil;
import data.DataUtil;
import data.LogUtil;
import data.SequenceUtil;
import data.StringUtil;
import graph.dm.Cluster;
import graph.dm.ClusterNode;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;
import graph.ext.persist.neo4j.Neo4JRESTConnection;
import graph.ext.persist.neo4j.Neo4JRESTConnection.CypherResultType;
import graph.ext.persist.neo4j.Neo4JUtil;
import graph.io.GraphConverter;
import graph.io.GraphMetrics;
import graph.util.GraphUtil;
import jee.rest.RESTDispatchServlet;
import jee.rest.RESTMethod;
import json.JSONObject;
import nl.ordina.tools.soa.soatool.SOATool;
import object.ObjectIterator;
import tool.Tool;

public class SOAToolRESTServlet extends RESTDispatchServlet {
	private static final long serialVersionUID = 4831364817779863550L;
	public static final String SOATOOL_ARG_FUNCTION = "func";
	public static final String SOATOOL_ARG_MESSAGE = "message";
	public static final String SOATOOL_NEO4J_QUERIES = "soatool.neo4j.queries";
	public static final String SOA_TOOL_ORCL_POST_ANALYSIS_QUERIES = "soatool.orcl.post.analysis.queries";
	public static final String SOATOOL_NEO4J_REST_ENDPOINT = "soatool.neo4j.rest.endpoint";
	public static final String SOATOOL_NEO4J_REST_USER = "soatool.neo4j.rest.user";
	public static final String SOATOOL_NEO4J_REST_PASSWORD = "soatool.neo4j.rest.password";
	public static final String SOATOOL_NEO4J_QUERY = "soatool.neo4j.query";
	public static final String SOATOOL_GV_OUTPUT_FILE_SEQUENCE = "gv.output";

	@Override
	protected DispatchContext getDispatchContext(String configuration) {
		Tool.readConfiguration(configuration);
		DispatchContext result = super.getDispatchContext(configuration);

		return result;
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "/list.*")
	public String[] list(DispatchContext cfg, HttpServletRequest req, HttpServletResponse res) {
		DummyTool tool = new DummyTool();
		String[] configurations = tool.getConfigurations();

		return configurations;
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "/file.*")
	public String file(DispatchContext cfg, HttpServletRequest req, HttpServletResponse res) {
		DummyTool tool = new DummyTool();
		String file = tool.getSetting(Tool.DEST_DIR, req.getParameter(Tool.ARG_FUNCTION))
				+ req.getPathInfo().substring("/file".length());
		res.setContentType(getFileContentType(file));
		LogUtil.getInstance().info("reading file [" + file + "]");
		return DataUtil.getInstance().readFromFile(file);
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "/log.*")
	public String[] log(DispatchContext cfg, HttpServletRequest req, HttpServletResponse res) {
		String message = getParameter(req, SOATOOL_ARG_MESSAGE);
		LogUtil.getInstance().info(message);
		return new String[] { message };
	}

	@RESTMethod(dispatchType = DispatchType.ASYNCH, URLPattern = "/init.*")
	public String[] initNeo4J(DispatchContext cfg, Parameters parameters) {		
		LogUtil.getInstance().info("intialize neo4j connection");
		Neo4JRESTConnection neo4jConn = new Neo4JRESTConnection(
				ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_REST_ENDPOINT),
				ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_REST_USER),
				ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_REST_PASSWORD));
		LogUtil.getInstance().info("settint intial password");
		neo4jConn.setInitialPassword();
		String postAnalysisQueries = ConfigurationUtil.getInstance().getSetting(SOA_TOOL_ORCL_POST_ANALYSIS_QUERIES);
		if (null != postAnalysisQueries && !postAnalysisQueries.isEmpty()) {
			for (String query : postAnalysisQueries.split(","))				
				neo4jConn.cypher(
						ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_QUERY + "." + query + ".cypher"));
		}
		return new String[] { "OK" };
	}

	@RESTMethod(dispatchType = DispatchType.ASYNCH, URLPattern = "/soatool*")
	public void soatool(DispatchContext cfg, Parameters parameters) {
		SOATool tool = new SOATool();
		tool.run(new String[] { "-" + Tool.ARG_FUNCTION, getFunction(parameters), "-" + Tool.ARG_MODE,
				Tool.ARG_MODE_SERVER });
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "/query/list")
	public Object queryList(DispatchContext cfg, HttpServletRequest req, HttpServletResponse res) {
		return ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_QUERIES).split(",");
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "(/query/gv)|(query/csv)")
	public List<String> queryExecute(DispatchContext cfg, HttpServletRequest req, HttpServletResponse res) {
		List<String> result = new ArrayList<>();
		String function = req.getParameter(Tool.ARG_FUNCTION);
		ByteArrayOutputStream bosCypher = new ByteArrayOutputStream();
		LogUtil.getInstance().info("start query [" + function + "]");
		String targetdir = ConfigurationUtil.getInstance().getSetting(Tool.DEST_DIR) + DataUtil.PATH_SEPARATOR
				+ SOATool.REST_REPORTS_DIR + DataUtil.PATH_SEPARATOR
				+ SequenceUtil.getInstance().getNext(SOATOOL_GV_OUTPUT_FILE_SEQUENCE);
		DataUtil.getInstance().makeDirectories(targetdir + DataUtil.PATH_SEPARATOR);

		SOATool tool = new SOATool();
		List<String> files = new ArrayList<>();
		String query = null;

		try {

			if (null != function && !function.isEmpty())
				query = ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_QUERY + "." + function + ".cypher");
			if (null == query || query.isEmpty()) {
				DataUtil.getInstance().copy(req.getInputStream(), bosCypher);
				query = bosCypher.toString();
			}

			Neo4JRESTConnection neo4jConn = new Neo4JRESTConnection(
					ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_REST_ENDPOINT),
					ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_REST_USER),
					ConfigurationUtil.getInstance().getSetting(SOATOOL_NEO4J_REST_PASSWORD));
			// if REST context path ends with csv OR the .type is row THEN
			// return a CSV
			CypherResultType resultType = (cfg.getContextPath().endsWith("csv")
					|| ConfigurationUtil.getInstance()
							.getSetting(SOATOOL_NEO4J_QUERY + "." + function + ".type",
									CypherResultType.GRAPH.getNeo4JType())
							.equals(CypherResultType.CSV.getNeo4JType())) ? CypherResultType.CSV
									: CypherResultType.GRAPH;
			JSONObject json = neo4jConn.cypher(query, resultType);
			LogUtil.getInstance().info("convert query result to " + resultType.name() + " [" + function + "]");
			switch (resultType) {
			case CSV:
				files = getCSVFileResult(tool, function, targetdir, json);
				break;
			case GRAPH:
				files = getGraphFileResult(tool, function, targetdir, json);
				break;
			}
		} catch (IOException e) {
			LogUtil.getInstance().info("unable to execute query", e);
		}
		for (String file : files) {
			result.add(tool.convertToURL(file, function));
		}
		LogUtil.getInstance().info("done query [" + function + "]");
		return result;
	}

	private List<String> getGraphFileResult(SOATool tool, String function, String targetdir, JSONObject json) {
		List<String> result = new ArrayList<>();
		Graph<Node, Edge<Node>> gra;
		List<Cluster<Node>> clusters = new ArrayList<>();

		gra = Neo4JUtil.getInstance().convertNeo4JResult2Graph(json);
		// create cluster nodes based on the group information
		String groupReplaceConfig = ConfigurationUtil.getInstance()
				.getSetting(SOATOOL_NEO4J_QUERY + "." + function + ".groups.regex");
		String defaultGroup = ConfigurationUtil.getInstance()
				.getSetting(SOATOOL_NEO4J_QUERY + "." + function + ".groups.default");
		if (null != groupReplaceConfig && !groupReplaceConfig.isEmpty()) {
			LogUtil.getInstance().info("determine cluster information [" + function + "]");
			Map<String, Cluster<Node>> mapClusters = new HashMap<>();
			String fieldProperty = SOATOOL_NEO4J_QUERY + "." + function + ".groups" + ".field";
			String field = ConfigurationUtil.getInstance().getSetting(fieldProperty, "id");
			int logCount = 0;
			for (Node node : gra.getNodes()) {
				String group = defaultGroup;
				try {
					Field f = ObjectIterator.lookupFields(Node.class, node.getClass()).get(field);
					String tmp = node.getClass().getSimpleName() + ":" + f.get(node).toString();
					group = StringUtil.getInstance().replace(tmp, groupReplaceConfig);
					if (tmp.equals(group))
						group = defaultGroup;
				} catch (Exception e) {
					if (logCount++ == 0)
						LogUtil.getInstance().info("unable to determine cluster information [" + function + "], check ["
								+ fieldProperty + "]", e);
				}
				if (null != group) {
					Cluster<Node> clu = mapClusters.get(group);
					if (null == clu) {
						clu = new Cluster<Node>();
						clu.setName(group);
						clu.setDescription(group);
						mapClusters.put(group, clu);
						clusters.add(clu);
					}
					clu.addNode(node);
				}
			}
		}
		String name = function;
		gra.setName(name);

		GraphMetrics gm = new GraphMetrics();
		String[] nodeExclusions = ConfigurationUtil.getInstance()
				.getSetting(SOATOOL_NEO4J_QUERY + "." + function + ".exclude.filter").split(",");
		LogUtil.getInstance().info("write result graph to file [" + function + "]");
		gm.writeGraphWithCycleInfo(targetdir, gra, clusters, tool.getNodeMarkup(), nodeExclusions);
		result.add(targetdir + DataUtil.PATH_SEPARATOR + gra.getName() + ".gv.svg");
		if (!clusters.isEmpty()) {
			LogUtil.getInstance().info("determine graph clusters  [" + function + "]");
			Graph<ClusterNode<Node>, Edge<ClusterNode<Node>>> graCluster = GraphUtil.getInstance()
					.createGraphForClusters(gra, clusters, null, null);
			graCluster.setName(name + "_cluster");
			LogUtil.getInstance().info("write result cluster graph to file [" + function + "]");
			gm.writeGraphWithCycleInfo(targetdir, graCluster, tool.getNodeMarkup());
			result.add(targetdir + DataUtil.PATH_SEPARATOR + graCluster.getName() + ".gv.svg");
		}
		LogUtil.getInstance().info("determine meta graph [" + function + "]");
		Graph<Node, Edge<Node>> graMeta = GraphUtil.getInstance().createMetaGraph(gra);
		LogUtil.getInstance().info("write meta graph to file [" + function + "]");
		gm.writeGraphWithCycleInfo(targetdir, graMeta, tool.getNodeMarkup());
		result.add(targetdir + DataUtil.PATH_SEPARATOR + graMeta.getName() + ".gv.svg");
		LogUtil.getInstance().info("perform graph conversion [" + function + "]");
		GraphConverter graCvt = new GraphConverter();
		graCvt.convertGV2GML_PNG_SVG(targetdir, false);

		return result;
	}

	private List<String> getCSVFileResult(SOATool tool, String function, String targetdir, JSONObject json) {
		List<String> result = new ArrayList<>();
		CSVData csv = Neo4JUtil.getInstance().convertNeo4JResult2CSV(json);

		String filename = targetdir + DataUtil.PATH_SEPARATOR + function + ".csv";
		CSVUtil.getInstance().writeToFile(filename, csv, ';');
		result.add(filename);

		return result;
	}

	private String getFunction(Parameters parameters) {
		String[] values = parameters.get(Tool.ARG_FUNCTION);
		String functions = "";
		if (null != values) {
			for (String val : values) {
				if (!functions.isEmpty())
					functions += ",";
				functions += val;
			}
		}
		return functions;
	}

	private class DummyTool extends Tool {
		@Override
		public void dispatch(String functions, String configuration, String sourcedir, String targetdir,
				String sourcefile, String[] args, Option... options) {
		}
	};
}
