package replace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import data.LogUtil;
import data.StringUtil;
import json.JSONObject;
import json.JSONUtil;
import json.JSONValue;
import json.Path;

/**
 * This object can be used to replace ${context variable/context path}
 * occurences in a text with specific context instance values.
 * 
 * The ${variable} logic of {@link data.StringUtil} is used. The replace context
 * itself is a JSON object. The <variable> is defined by <context
 * variablename>/<JSON path>. So every path that is defined by JSON is also
 * allowed here.
 * 
 * This object stores the context objects itself in a map AND performs the
 * replacement (by using the {@link data.StringUtil} class)
 * 
 * @author mwa17610
 *
 */
public class ReplaceContext {
	private Map<String, Context> context;

	public ReplaceContext(String context, Object object) {
		this.init(new String[] { context }, new Object[] { object });
	}

	public ReplaceContext(String[] context, Object[] objects) {
		this.init(context, objects);
	}

	public ReplaceContext(Map<String, Object> context) {
		this.init(context);
	}

	protected void init(String[] context, Object[] objects) {
		if (null == this.context)
			this.context = new HashMap<>();
		int idx = 0;
		for (String ctx : context)
			this.context.put(ctx, new Context(objects[idx++]));

	}

	protected void init(Map<String, Object> context) {
		if (null == this.context)
			this.context = new HashMap<>();
		for (Entry<String, Object> e : context.entrySet())
			this.context.put(e.getKey(), new Context(e.getValue()));
	}

	public JSONObject getJSON(String fullpath) {
		String context = getContextVariableName(fullpath);
		JSONObject result = null;
		Context ctx;
		if (null != context && !context.isEmpty())
			ctx = this.context.get(context);
		else
			ctx = (this.context.size() == 1) ? this.context.values().iterator().next() : null;

		if (null != ctx)
			result = ctx.getJSONObject();
		else
			LogUtil.getInstance().warning("unable to get JSONObject for [" + fullpath + "]");

		return result;
	}

	public String getContextVariableName(String fullpath) {
		String result = null;

		int idx = fullpath.indexOf("/");

		if (idx > 0)
			result = fullpath.substring(0, idx);
		else
			result = fullpath;

		return result;
	}

	public String getContextPath(String fullpath) {
		String result = null;

		int idx = fullpath.indexOf("/");

		if (idx > 0)
			result = fullpath.substring(idx + 1);
		else
			result = fullpath;

		return result;
	}

	/**
	 * Internal representation of a context object.
	 * 
	 * The context is transformed to a JSON object.
	 * 
	 * @author mwa17610
	 *
	 */
	class Context {
		public Context(Object object) {
			this.object = object;

		}

		private Object object;
		private JSONObject json;

		public JSONObject getJSONObject() {
			if (null == json)
				json = JSONUtil.getInstance().java2JSON(object);
			return json;
		}
	}

	public String replace(String str) {
		String result = StringUtil.getInstance().replace(str, (var) -> {
			String path = this.getContextPath(var);
			JSONObject json = this.getJSON(var);

			if (null != json && null != path && !path.isEmpty()) {
				Path jsonPath = Path.newPath(path);
				JSONObject value = JSONUtil.getInstance().getPath(json, jsonPath);
				return (value instanceof JSONValue) ? ((JSONValue<?>) value).getStringValue() : "";
			} else
				return "";
		});

		return result;
	}

}
