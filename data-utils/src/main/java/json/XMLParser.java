package json;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;

import xml.XMLSAXHandler;
import xml.XMLUtil;

public class XMLParser {
	public JSONObject parse(String xml) {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
		XMLJSONHandler handler = new XMLJSONHandler();
		XMLUtil.getInstance().parse(is, handler);

		return handler.getRoot();
	}

	class Values {
		List<JSONObject> values;

		public Values() {

		}

		public List<JSONObject> getValues() {
			return values;
		}

		public void addValue(String data) {
			values.add(new JSONValue<String>(data));
		}

		public JSONObject makeJSONObject() {
			JSONObject result = null;
			if (values.size() == 1) {
				result = values.get(0);
			} else {
				JSONList list;
				result = list = new JSONList();
				for (JSONObject value : values)
					list.getData().add(value);
			}
			return result;
		}
	}

	class XMLJSONHandler extends XMLSAXHandler {
		JSONRecord root;
		Stack<JSONRecord> recordStack;
		int prevDepth;

		public XMLJSONHandler() {
			root = new JSONRecord();
			recordStack = new Stack<>();
			recordStack.push(root);
			prevDepth = 0;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			prevDepth = getDepth();
			super.startElement(uri, localName, qName, atts);
			JSONRecord current = new JSONRecord();
			recordStack.push(current);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			JSONRecord current = recordStack.pop();
			current.clean();
			if (!current.isEmpty())
				recordStack.peek().add(localName, current);
			else
				recordStack.peek().add(localName, new JSONValue<String>(getData()));

			super.endElement(uri, localName, qName);
		}

		public JSONRecord getRoot() {
			return root;
		}
	}
}
