package json;

public class JSONQuery {
	JSONObject json;

	public JSONQuery(JSONObject json) {
		this.json = json;
	}

	public JSONQuery get(String key) {
		JSONQuery result = null;
		if (json instanceof JSONRecord && !isEmpty())
			result = new JSONQuery(((JSONRecord) json).getData().get(key));

		if (null == result)
			result = new JSONQuery(null);

		return result;
	}

	public JSONQuery get(int idx) {
		JSONQuery result = null;
		if (idx >= 0 && json instanceof JSONList && !isEmpty() && ((JSONList) json).getData().size() > idx)
			result = new JSONQuery(((JSONList) json).getData().get(idx));

		if (null == result)
			result = new JSONQuery(null);

		return result;
	}

	public int length() {
		int result = -1;

		if (json instanceof JSONList) {
			if (!isEmpty())
				result = ((JSONList) json).getData().size();
			else
				result = 0;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends JSONObject> T get() {
		return (T) json;
	}

	public boolean isEmpty() {
		return null == json || null == json.getData();
	}
}
