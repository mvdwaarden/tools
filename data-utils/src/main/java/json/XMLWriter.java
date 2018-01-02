package json;

import java.util.Map;

import xml.XMLUtil;

public class XMLWriter {

	public String write(JSONObject jsonObject, String roottag) {

		XMLWriterCallback cb = new XMLWriterCallback();
		cb.roottag = roottag;

		JSONWalker walker = new JSONWalker();

		walker.walk(jsonObject, cb);

		return cb.doc.toString();
	}

	class XMLWriterCallback extends JSONWalkerCallbackAdapter {
		int indent;
		String roottag;
		StringBuilder doc = new StringBuilder();

		@Override
		public void onBeginRecursion(String tag, JSONObject parent, JSONObject object) {
			if (!(object instanceof JSONList)) {
				indent(doc, indent);
				doc.append("<" + ((null == tag) ? roottag : tag) + ">");
			}
			++indent;
		}

		@Override
		public void onEndRecursion(String tag, JSONObject parent, JSONObject object) {
			--indent;
			if (!(object instanceof JSONList)) {
				if (object instanceof Map)
					indent(doc, indent);
				doc.append("</" + ((null == tag) ? roottag : tag) + ">");
			}
		}

		@Override
		public void onAfterListItem(String tag, JSONObject object, int idx) {
			if (object instanceof JSONValue)
				doc.append(" ");
		}

		@Override
		public void onValue(String tag, JSONValue<?> value) {
			if (value.getData() instanceof String) {
				doc.append(XMLUtil.getInstance().escapeXML((String) value.getData()));
			} else {
				doc.append(value.getStringValue());
			}
		}		
	}

}
