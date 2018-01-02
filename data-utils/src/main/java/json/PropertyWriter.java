package json;

public class PropertyWriter {

	public String write(JSONObject jsonObject) {
		PropertyWriterCallback cb = new PropertyWriterCallback();
		JSONWalker walker = new JSONWalker();
		walker.walk(jsonObject, cb);
		String result = cb.doc.toString();

		return result;
	}

	class PropertyWriterCallback extends JSONWalkerCallbackAdapter {
		Path path = new Path();
		StringBuilder doc = new StringBuilder();

		@Override
		public void onBeginRecursion(String tag, JSONObject parent, JSONObject object) {
			if (parent instanceof JSONList)
				path.pushName(null);
			else
				path.pushName(tag);
		}

		@Override
		public void onBeforeListItem(String tag, JSONObject object, int idx) {
			path.pushIndex(idx);
		}

		@Override
		public void onEndRecursion(String tag, JSONObject parent, JSONObject object) {
			path.pop();
		}

		@Override
		public void onAfterListItem(String tag, JSONObject object, int idx) {
			path.pop();
		}

		@Override
		public void onValue(String tag, JSONValue<?> value) {
			doc.append(path.getPathString() + "=" + value.getStringValue());
			doc.append("\r");
		}
	}
}
