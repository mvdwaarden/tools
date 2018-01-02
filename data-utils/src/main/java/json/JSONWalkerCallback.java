package json;

public interface JSONWalkerCallback {
	void onStart();

	void onEnd();

	/*
	 * If the parent is a JSONList, the tag applies to the tag of the list (the
	 * parent) and not the object)
	 */
	void onBeginRecursion(String tag, JSONObject parent, JSONObject object);

	void onEndRecursion(String tag, JSONObject parent, JSONObject object);

	void onBeforeRecord(String tag, JSONRecord record);

	void onBeforeRecordItem(String tag, JSONObject object);

	void onRecordItem(String tag, JSONObject object);

	void onAfterRecordItem(String tag, JSONObject object);

	void onAfterRecord(String tag, JSONRecord record);

	void onBeforeList(String tag, JSONList list);

	void onAfterList(String tag, JSONList list);

	void onBeforeListItem(String tag, JSONObject object, int idx);

	void onListItem(String tag, JSONObject object, int idx);

	void onAfterListItem(String tag, JSONObject object, int idx);

	void onBeforeValue(String tag, JSONValue<?> value);

	void onValue(String tag, JSONValue<?> value);

	void onNull(String tag);

	void onAfterValue(String tag, JSONValue<?> value);

	void onRecordRef(JSONRecord object);

	void onListRef(JSONList object);
}
