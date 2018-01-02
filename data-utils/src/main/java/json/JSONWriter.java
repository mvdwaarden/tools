package json;

import data.StringUtil;

public class JSONWriter {

	public String write(JSONObject jsonObject) {

		JSONWriterCallback cb = new JSONWriterCallback();

		JSONWalker walker = new JSONWalker();

		walker.walk(jsonObject, cb);

		return cb.doc.toString();
	}

	class JSONWriterCallback extends JSONWalkerCallbackAdapter {
		int indent;
		StringBuilder doc = new StringBuilder();

		@Override
		public void onEnd() {
			StringUtil.getInstance().stripEnd(doc, ",");
		}

		@Override
		public void onBeginRecursion(String tag, JSONObject parent, JSONObject object) {
			indent(doc, indent);
			if (null != tag && !(parent instanceof JSONList))
				doc.append("\"" + tag + "\" : ");
			++indent;
		}

		@Override
		public void onEndRecursion(String tag, JSONObject parent, JSONObject object) {
			--indent;
		}

		@Override
		public void onBeforeRecord(String tag, JSONRecord record) {
			doc.append("{");
		}

		@Override
		public void onAfterRecord(String tag, JSONRecord record) {
			StringUtil.getInstance().stripEnd(doc, ",");
			indent(doc, indent);
			doc.append("}");
			doc.append(",");
		}

		@Override
		public void onBeforeList(String tag, JSONList list) {
			doc.append("[");
		}

		@Override
		public void onAfterList(String tag, JSONList list) {
			StringUtil.getInstance().stripEnd(doc, " ");
			StringUtil.getInstance().stripEnd(doc, ",");
			indent(doc, indent);
			doc.append("]");
			doc.append(",");
		}

		@Override
		public void onAfterListItem(String tag, JSONObject object, int idx) {
			if (object instanceof JSONValue)
				doc.append(" ");
		}

		@Override
		public void onValue(String tag, JSONValue<?> value) {
			if (value.getData() instanceof String) {
				doc.append("\"" + JSONUtil.getInstance().escapeJSON((String) value.getData()) + "\"");
			} else {
				doc.append(value.getStringValue());
			}
			doc.append(",");
		}
		@Override
		public void onNull(String tag) {
			doc.append(",");			
		}
	}
}
