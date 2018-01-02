package json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JSONRecord implements JSONObject {
	private Map<String, JSONObject> data;
	@Override
	public Map<String, JSONObject> getData() {
		if (null == data)
			data = new LinkedHashMap<>();

		return data;
	}

	public void add(String key, JSONObject jsonObject) {
		JSONObject obj = getData().get(key);

		if (null == obj) {
			getData().put(key, jsonObject);
		} else if (obj instanceof JSONList) {
			((JSONList) obj).getData().add(jsonObject);
		} else {
			JSONList list = new JSONList();
			list.getData().add(obj);
			list.getData().add(jsonObject);
			getData().remove(key);
			getData().put(key, list);
		}
	}
	
	public void clean() {
		if (!isEmpty()) {
			List<String> toRemove = new ArrayList<>();
			for (Entry<String, JSONObject> e : data.entrySet()) {
				if (e.getValue() instanceof JSONRecord && ((JSONRecord) e.getValue()).isEmpty())
					toRemove.add(e.getKey());

			}
			for (String key : toRemove)
				data.remove(key);
		}
	}
	@Override
	public boolean isEmpty() {
		return null == data || data.isEmpty();
	}
}
