package rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import data.LogUtil;
import data.StringUtil;
import json.JSONObject;
import json.JSONUtil;
import xml.XMLUtil;

public class RESTClient {
	public static final String ACCEPT = "Accept";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String AUTHORIZATION = "Authorization";
	private HttpClient client;
	private Map<String, String> headers = new HashMap<>();
	private Authentication authentication = new NoAuthentication();

	public RESTClient() {

	}

	public RESTClient setHeaders(String[][] headers) {
		for (String[] header : headers)
			this.headers.put(header[0], header[1]);
		return this;
	}

	public RESTClient setAuthorization(String user, String password) {
		authentication = new BasicAuthentication(user, password);

		return this;
	}

	public String entity2String(HttpEntity entity) {
		String result = "";
		try {
			result = EntityUtils.toString(entity);
		} catch (ParseException | IOException e) {
			LogUtil.getInstance().error("could not convert entity to string", e);
		}

		return result;
	}

	public InputStream entity2Content(HttpEntity entity) {
		InputStream result = null;
		try {
			result = entity.getContent();
		} catch (ParseException | IOException e) {
			LogUtil.getInstance().error("could not get entity content", e);
		}

		return result;
	}

	public InputStream post(String endpoint, String[][] args, JSONObject jsonObject) {
		return execute(new HttpPost(getUri(endpoint, args)), jsonObject, e -> entity2Content(e));
	}

	public <R> R post(String endpoint, String[][] args, JSONObject jsonObject, Function<HttpEntity, R> map) {
		return execute(new HttpPost(getUri(endpoint, args)), jsonObject, map);
	}

	public String get(String endpoint, String[][] args) {
		return execute(new HttpGet(getUri(endpoint, args)), null);
	}

	protected String execute(HttpUriRequest request, JSONObject jsonObject) {
		return execute(request, jsonObject, e -> entity2String(e));
	}

	protected <R> R execute(HttpUriRequest request, JSONObject jsonObject, Function<HttpEntity, R> map) {
		R result = null;

		HttpResponse response;
		try {
			for (Entry<String, String> header : headers.entrySet()) {
				request.setHeader(header.getKey(), header.getValue());
			}
			authentication.doAuthentication(request);
			// set content
			if (null != jsonObject) {
				StringEntity entity = new StringEntity(JSONUtil.getInstance().writeJSON(jsonObject));
				if (request instanceof HttpEntityEnclosingRequest)
					((HttpEntityEnclosingRequest) request).setEntity(entity);
			}
			response = getClient().execute(request);
			if (null != response.getEntity()) {
				result = map.apply(response.getEntity());
				LogUtil.getInstance().info("received [" + response.getEntity().getContentLength() + "] bytes");
			}
		} catch (IOException e) {
			LogUtil.getInstance().error("problem with http GET on endpoint [" + request.getURI().toString() + "]", e);
		}

		return result;
	}

	public String get(String endpoint, String[][] args, Function<HttpEntity, String> map) {
		return execute(new HttpGet(getUri(endpoint, args)), null, map);
	}

	public String put(String endpoint, String[][] args, JSONObject jsonObject) {
		return execute(new HttpPost(getUri(endpoint, args)), jsonObject);
	}

	public HttpClient getClient() {
		if (null == client)
			client = HttpClientBuilder.create().build();

		return client;
	}

	public URI getUri(String endpoint, String[][] parameters) {
		StringBuilder result = new StringBuilder(endpoint);

		if (parameters.length > 0) {
			result.append("?");
			for (String[] param : parameters)
				result.append(param[0] + "=" + param[1] + "&");
			StringUtil.getInstance().stripEnd(result, "&");
		}
		URI uri = null;
		try {
			uri = new URI(result.toString());
		} catch (URISyntaxException e) {
			LogUtil.getInstance().error("unable to create URI from [" + result.toString() + "]", e);
		}

		return uri;
	}

	private interface Authentication {
		void doAuthentication(HttpRequest request);
	}

	private class NoAuthentication implements Authentication {
		@Override
		public void doAuthentication(HttpRequest request) {

		}
	}

	private class BasicAuthentication implements Authentication {
		private String user;
		private String password;

		public BasicAuthentication(String user, String password) {
			this.user = user;
			this.password = password;
		}

		public String getAuthorizationValue() {
			String auth = user + ":" + password;
			return "Basic " + XMLUtil.getInstance().cvtBytes2Base64(auth.getBytes());
		}

		public void doAuthentication(HttpRequest request) {
			String value = getAuthorizationValue();
			request.addHeader(AUTHORIZATION, value);
		}
	}
}
