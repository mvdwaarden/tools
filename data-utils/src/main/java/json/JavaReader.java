package json;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import algoritm.RecursionControl;
import object.ObjectIterator;
import object.ObjectUtil;
import object.ObjectUtil.Option;

/**
 * Reads a Java object into a JSONObject structure.
 * 
 * @author mwa17610
 *
 */
public class JavaReader {
	public static final String NILL_KEY = "<nill>";

	public JSONObject read(Object obj) {
		return read(new RecursionControl<Object, JSONObject>(new JSONValue<Boolean>(true)), obj);
	}

	private JSONObject read(RecursionControl<Object, JSONObject> ctrl, Object obj) {
		JSONObject result = null;

		if (isLeaf(obj)) {
			result = getJSONValue(obj);
		} else if (isMap(obj)) {
			if (ctrl.doRecursion(obj)) {
				JSONRecord record;
				result = record = new JSONRecord();
				ctrl.put(obj, record);
				for (Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
					if (isLeaf(e.getValue()))
						record.add(toKeyString(e.getKey()), getJSONValue(e.getValue()));
					else if (null != e.getValue())
						record.add(toKeyString(e.getKey()), read(ctrl, e.getValue()));
				}
			} else {
				result = ctrl.get(obj);
			}
		} else if (isArray(obj)) {
			if (ctrl.doRecursion(obj)) {
				JSONList list;
				result = list = new JSONList();
				ctrl.put(obj, result);
				if (obj instanceof Iterable) {
					for (Object i : (Iterable<?>) obj)
						list.getData().add(read(ctrl, i));
				} else if (obj instanceof Object[]) {
					for (Object i : (Object[]) obj) {
						list.getData().add(read(ctrl, i));
					}
				}
			} else {
				result = ctrl.get(obj);
			}
		} else if (null != obj) {
			if (ctrl.doRecursion(obj)) {
				ObjectIterator it = new ObjectIterator(obj);

				JSONRecord record;
				result = record = new JSONRecord();
				ctrl.put(obj, record);

				for (Entry<String, Object> e : it.map().entrySet()) {
					if (isLeaf(e.getValue()))
						record.add(toKeyString(e.getKey()), getJSONValue(e.getValue()));
					else if (null != e.getValue())
						record.add(toKeyString(e.getKey()), read(ctrl, e.getValue()));
				}
			} else {
				result = ctrl.get(obj);
			}
		}

		return result;
	}

	private String toKeyString(Object key) {
		if (key instanceof String)
			return (String) key;
		else if (null != key)
			return key.toString();
		else
			return NILL_KEY;
	}

	public JSONValue<?> getJSONValue(Object obj) {
		return DataConverter.getInstance().makeJSONValue(obj);
	}

	public boolean isLeaf(Object obj) {
		return ObjectUtil.getInstance().isPrimitive(obj, Option.FLOATING_POINT) || obj instanceof String
				|| obj instanceof Date;

	}

	public boolean isArray(Object obj) {
		return obj instanceof List || obj instanceof Object[];

	}

	public boolean isMap(Object obj) {
		return obj instanceof Map;

	}
}
