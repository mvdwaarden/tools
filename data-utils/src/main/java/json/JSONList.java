package json;

import java.util.ArrayList;
import java.util.List;

public class JSONList implements JSONObject {
	private List<JSONObject> data;
	@Override
	public List<JSONObject> getData() {
		if (null == data)
			data = new ArrayList<>();

		return data;
	}
	@Override
	public boolean isEmpty(){
		return null == data;
	}
}
