package json;

import java.util.Map.Entry;

import algoritm.RecursionControl;

public class JSONWalker {

	public JSONWalkerCallback walk(JSONObject jsonObject, JSONWalkerCallback callback) {
		callback.onStart();
		_walk(new RecursionControl<Object, JSONObject>(new JSONValue<Boolean>(true)), callback, null, jsonObject, null,
				1);
		callback.onEnd();

		return callback;
	}

	private void _walk(RecursionControl<Object, JSONObject> ctx, JSONWalkerCallback callback, JSONObject parent,
			JSONObject jsonObject, String tag, int indent) {

		callback.onBeginRecursion(tag, parent, jsonObject);

		if (null != jsonObject && !jsonObject.isEmpty()) {
			if (jsonObject instanceof JSONList) {
				if (ctx.doRecursion(jsonObject)) {
					callback.onBeforeList(tag, (JSONList) jsonObject);
					int i = 0;
					for (JSONObject o : ((JSONList) jsonObject).getData()) {
						callback.onBeforeListItem(tag, o, i);
						callback.onListItem(tag, o, i);
						_walk(ctx, callback, jsonObject, o, tag, indent);
						callback.onAfterListItem(tag, o, i);
						++i;
					}
					callback.onAfterList(tag, (JSONList) jsonObject);
				} else {
					callback.onListRef((JSONList) jsonObject);
				}
			} else if (jsonObject instanceof JSONRecord) {
				if (ctx.doRecursion(jsonObject)) {
					callback.onBeforeRecord(tag, (JSONRecord) jsonObject);
					for (Entry<String, JSONObject> e : ((JSONRecord) jsonObject).getData().entrySet()) {
						callback.onBeforeRecordItem(e.getKey(), e.getValue());
						callback.onRecordItem(e.getKey(), e.getValue());
						_walk(ctx, callback, jsonObject, e.getValue(), e.getKey(), indent + 1);
						callback.onAfterRecordItem(e.getKey(), e.getValue());
					}
					callback.onAfterRecord(tag, (JSONRecord) jsonObject);
				} else {
					callback.onRecordRef((JSONRecord) jsonObject);
				}
			} else if (jsonObject instanceof JSONValue) {
				callback.onValue(tag, (JSONValue<?>) jsonObject);
			}
		} else if (null == jsonObject) {
			callback.onNull(tag);
		}
		callback.onEndRecursion(tag, parent, jsonObject);
	}
}
