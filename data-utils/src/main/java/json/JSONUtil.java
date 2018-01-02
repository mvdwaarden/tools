package json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import data.EnumUtil;
import data.LogUtil;
import data.Util;
import json.Path.Option;

public class JSONUtil implements Util {
	private static final ThreadLocal<JSONUtil> instance = new ThreadLocal<JSONUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static JSONUtil getInstance() {
		JSONUtil result = instance.get();

		if (null == result) {
			result = new JSONUtil();
			instance.set(result);
		}

		return result;
	}

	public String json2XML(String json, String roottag) {
		JSONObject jsonObject = parseJSON(json);
		XMLWriter wri = new XMLWriter();

		return wri.write(jsonObject, roottag);
	}

	public String xml2JSON(String xml) {
		JSONObject jsonObject = new XMLParser().parse(xml);

		JSONWriter wri = new JSONWriter();

		return wri.write(jsonObject);
	}

	public String writeJSON(JSONObject jsonObject) {
		JSONWriter wri = new JSONWriter();
		String result = wri.write(jsonObject);

		return result;
	}

	public String writeXML(JSONObject jsonObject, String root) {
		XMLWriter wri = new XMLWriter();
		String result = wri.write(jsonObject, root);

		return result;
	}

	public JSONObject parseJSON(String json) {
		JSONParser parser = new JSONParser();

		JSONObject jsonObject = parser.parse(json);

		return jsonObject;

	}
	
	public JSONObject parseJSON(InputStream json) {
		JSONParser parser = new JSONParser();

		JSONObject jsonObject = parser.parse(json);

		return jsonObject;

	}

	public JSONObject parseXML(String xml) {
		XMLParser parser = new XMLParser();

		JSONObject jsonObject = parser.parse(xml);

		return jsonObject;
	}

	public String escapeJSON(String str) {
		return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
	}

	public JSONObject java2JSON(Object obj) {
		JavaReader reader = new JavaReader();

		JSONObject jsonObject = reader.read(obj);

		return jsonObject;
	}

	public Object json2Java(JSONObject jsonObject, Object root) {
		JavaWriter wri = new JavaWriter();
		Object result = wri.write(jsonObject, root);

		return result;
	}

	public Properties json2Properties(JSONObject jsonObject) {
		PropertyWriter wri = new PropertyWriter();

		Properties result = new Properties();

		try {
			String tmp = wri.write(jsonObject);
			result.load(new ByteArrayInputStream(tmp.getBytes()));
		} catch (IOException e) {
			LogUtil.getInstance().error("problem making properties from JSON", e);
		}

		return result;
	}

	public JSONObject properties2JSON(Properties props) {
		PropertyReader reader = new PropertyReader();

		JSONObject result = reader.read(props);

		return result;
	}

	public JSONObject getPath(JSONObject object, String path, Option... options) {
		return getPath(object, Path.newPath(path), options);
	}

	public JSONObject getPath(JSONObject object, Path path, Option... options) {
		boolean create = EnumUtil.getInstance().contains(options, Option.MAKE_PATH);
		JSONObject currentObject = object;
		JSONObject nextObject = null;
		for (int i = 0; i < path.getParts().size(); ++i) {
			nextObject = null;
			Path.Part<?> currentPart = path.getParts().get(i);
			if (currentObject instanceof JSONList && currentPart instanceof Path.IndexPart) {
				JSONList list = (JSONList) currentObject;
				Path.IndexPart idxpart = (Path.IndexPart) currentPart;

				if (idxpart.getValue() >= 0 && idxpart.getValue() < list.getData().size())
					nextObject = list.getData().get(idxpart.getValue());
			} else if (currentObject instanceof JSONRecord && currentPart instanceof Path.NamePart) {
				JSONRecord record = (JSONRecord) currentObject;
				Path.NamePart namepart = (Path.NamePart) currentPart;
				nextObject = record.getData().get(namepart.getValue());
			}
			if (null != nextObject)
				currentObject = nextObject;
			else if (create)
				currentObject = nextObject = createNextPartObject(currentObject, currentPart,
						(i + 1 < path.getParts().size()) ? path.getParts().get(i + 1) : null);
			else
				currentObject = null;
			if (null == currentObject || currentObject instanceof JSONValue)
				break;
		}
		return nextObject;
	}

	public void setPath(JSONObject object, String path, String value, Option... options) {
		setPath(object, Path.newPath(path), value, options);
	}

	public <T> void setPath(JSONObject object, Path path, T value, Option... options) {
		@SuppressWarnings("unchecked")
		JSONValue<T> jsonValue = (JSONValue<T>) getPath(object, path, options);

		if (null != jsonValue)
			jsonValue.setData(value);
	}

	private JSONObject createNextPartObject(JSONObject parent, Path.Part<?> currentPart, Path.Part<?> nextPart) {
		JSONObject result = null;
		Path.IndexPart idxPart = null;
		Path.NamePart namePart = null;
		if (currentPart instanceof Path.IndexPart)
			idxPart = (Path.IndexPart) currentPart;
		else if (currentPart instanceof Path.NamePart)
			namePart = (Path.NamePart) currentPart;
		// create a new object
		if (nextPart instanceof Path.IndexPart) {
			result = new JSONList();
		} else if (nextPart instanceof Path.NamePart) {
			result = new JSONRecord();
		} else {
			result = new JSONValue<String>(null);
		}
		// put it in the parent
		if (null != result) {
			if (parent instanceof JSONList && null != idxPart) {
				JSONList list = (JSONList) parent;
				for (int i = list.getData().size(); i <= idxPart.getValue(); ++i)
					list.getData().add(null);
				list.getData().set(idxPart.getValue(), result);
			} else if (parent instanceof JSONRecord && null != namePart) {
				JSONRecord record = (JSONRecord) parent;
				record.getData().put(namePart.getValue(), result);
			}
		}

		return result;
	}
}
