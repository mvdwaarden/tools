package json;

public class JSONValue<T> implements JSONObject {
	private T data;

	public JSONValue(T data) {
		this.data = data;
	}

	@Override
	public T getData() {
		return data;
	}

	@Override
	public boolean isEmpty() {
		return null == data;
	}

	public void setData(T value) {
		this.data = value;
	}

	public String getStringValue() {
		String result = "";
		if (data instanceof Boolean) {
			result = String.valueOf((Boolean) data);
		} else if (data instanceof String) {
			result = (String) data;
		} else if (data instanceof Double) {
			result = String.valueOf((Double) data);
		} else if (data instanceof Long) {
			result = String.valueOf((Long) data);
		} else if (data instanceof Integer) {
			result = String.valueOf((Integer) data);
		}
		return result;
	}
}
