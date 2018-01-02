package json;

public class JSONWalkerCallbackAdapter implements JSONWalkerCallback {

	@Override
	public void onStart() {

	}

	@Override
	public void onEnd() {

	}

	@Override
	public void onBeginRecursion(String tag, JSONObject parent, JSONObject object) {

	}

	@Override
	public void onEndRecursion(String tag, JSONObject parent, JSONObject object) {

	}

	@Override
	public void onBeforeRecord(String tag, JSONRecord record) {

	}

	@Override
	public void onBeforeRecordItem(String tag, JSONObject object) {

	}

	@Override
	public void onRecordItem(String tag, JSONObject object) {

	}

	@Override
	public void onAfterRecordItem(String tag, JSONObject object) {

	}

	@Override
	public void onAfterRecord(String tag, JSONRecord record) {

	}

	@Override
	public void onBeforeList(String tag, JSONList list) {

	}

	@Override
	public void onAfterList(String tag, JSONList list) {

	}

	@Override
	public void onBeforeListItem(String tag, JSONObject object, int idx) {

	}

	@Override
	public void onListItem(String tag, JSONObject object, int idx) {

	}

	@Override
	public void onAfterListItem(String tag, JSONObject object, int idx) {

	}

	@Override
	public void onBeforeValue(String tag, JSONValue<?> value) {

	}

	@Override
	public void onValue(String tag, JSONValue<?> value) {

	}
	
	@Override
	public void onNull(String tag) {
		
	}

	@Override
	public void onAfterValue(String tag, JSONValue<?> value) {

	}

	@Override
	public void onListRef(JSONList object) {

	}

	@Override
	public void onRecordRef(JSONRecord object) {

	}

	public void indent(StringBuilder doc, int indent) {
		doc.append("\n");
		for (int i = 0; i < indent; ++i)
			doc.append("  ");
	}
}
