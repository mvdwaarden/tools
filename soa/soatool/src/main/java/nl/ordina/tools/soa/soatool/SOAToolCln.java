package nl.ordina.tools.soa.soatool;

import java.util.ArrayList;
import java.util.List;

import jee.rest.RESTDispatchServlet.DispatchType;
import jee.rest.RESTMethod;
import json.JSONObject;
import json.JSONRecord;
import json.JSONUtil;
import json.JSONValue;
import rest.RESTClient;

public class SOAToolCln {
	private RESTClient cln;
	private String host;
	private String configuration;

	public SOAToolCln(String host, String configuration) {
		this.host = host;
		this.configuration = configuration;
	}

	private void init() {
		if (null == cln) {
			cln = new RESTClient();
		}
	}

	private String buildEndpoint(String operation) {
		return host + "/soatoolsrv/" + operation;
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "/list.*")
	public String[] list() {
		init();
		JSONObject json = JSONUtil.getInstance().parseJSON(cln.post(buildEndpoint("/list"),
				new String[][] { { "func", "list" }, { "config", configuration } }, null));

		return new String[] {};

	}

	@RESTMethod(dispatchType = DispatchType.ASYNCH, URLPattern = "/init.*")
	public String[] initNeo4J() {
		init();
		JSONObject json = JSONUtil.getInstance().parseJSON(cln.post(buildEndpoint("/init/gv"),
				new String[][] { { "func", "queryInit" }, { "config", configuration } }, null));

		return new String[] {};

	}

	@RESTMethod(dispatchType = DispatchType.ASYNCH, URLPattern = "/soatool*")
	public void soatool() {
		init();
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "/query/list")
	public String[] queryList() {
		init();
		JSONRecord json = (JSONRecord) JSONUtil.getInstance().parseJSON(cln.post(buildEndpoint("/query/list"),
				new String[][] { { "func", "queryList" }, { "config", configuration } }, null));
		List<JSONValue> list = (List<JSONValue>) json.getData().get("list").getData();
		String[] result = new String[list.size()];
		int i = 0;
		for (JSONValue<String> val : list) {
			result[i++] = val.getData();
		}

		return result;
	}

	@RESTMethod(dispatchType = DispatchType.SYNCH, URLPattern = "(/query/gv)|(query/csv)")
	public List<String> queryExecute(String query) {
		init();
		JSONUtil.getInstance().parseJSON(cln.post(buildEndpoint("/query/gv"),
				new String[][] { { "func", query }, { "config", configuration } }, null));

		return new ArrayList<>();
	}
}
