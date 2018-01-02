package jee.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import data.DataUtil;
import json.JSONObject;
import json.JSONUtil;

public class RESTServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handle(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handle(req, res);
	}

	private void handle(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		InputStream is = req.getInputStream();
		res.setContentType("application/json");

		String json = DataUtil.getInstance().readFromInputStream(is, getCharset());
		req.getParameterMap();

		JSONObject jsonObject = JSONUtil.getInstance().parseJSON(json);
		Output out = new Output();
		out.input = json;
		out.pathInfo = req.getPathInfo();
		out.message = "receive this JSON input";
		out.parsedMessage.add(JSONUtil.getInstance().writeJSON(jsonObject));
		out.parsedMessage.add(JSONUtil.getInstance().writeXML(jsonObject, "root"));
		for (Object key : req.getParameterMap().keySet())
			out.parameterMap
					.add(new KeyValue(key.toString(), ((String[]) req.getParameterMap().get(key))[0].toString()));
		DataUtil.getInstance().writeToOutputStream(res.getOutputStream(),
				JSONUtil.getInstance().writeJSON(JSONUtil.getInstance().java2JSON(out)), getCharset());
	}

	class Output {
		String input;
		String message;
		List<String> parsedMessage = new ArrayList<>();
		String pathInfo;
		List<KeyValue> parameterMap = new ArrayList<>();
	}

	class KeyValue {
		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public KeyValue() {
		}

		String key;
		String value;
	}

	public Charset getCharset() {
		return null;
	}

	public String getFileContentType(String filename) {
		String result = "";
		String[][] map = new String[][] { { "csv", "text/csv" }, { "svg", "image/svg+xml" } };

		for (String[] cfg : map) {
			if (filename.endsWith(cfg[0])) {
				result = cfg[1];
			}
		}
		return result;
	}
}
