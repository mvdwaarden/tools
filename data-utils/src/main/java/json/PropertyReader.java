package json;

import java.util.Map.Entry;
import java.util.Properties;

import json.Path.Option;

public class PropertyReader {
	@SuppressWarnings("unchecked")
	public JSONObject read(Properties props) {
		JSONRecord result = new JSONRecord();
		
		for (Entry<?,?> e : props.entrySet()){
			Path path = Path.newPath((String)e.getKey());
			JSONObject object = JSONUtil.getInstance().getPath(result,path,Option.MAKE_PATH);
			if (object instanceof JSONValue)
				((JSONValue<String>) object).setData((String)e.getValue());
		}
		
		return result;
	}
}
