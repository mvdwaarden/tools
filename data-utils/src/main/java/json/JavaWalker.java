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
public class JavaWalker<T> {
	public static final String NILL_KEY = "<nill>";
	private JavaWalkerCallback<T> callback;

	public T walk(Object obj, T initial, JavaWalkerCallback<T> callback) {
		this.callback = callback;
		this.callback.onStart(obj,initial);
		walk(new RecursionControl<Object, T>(initial), obj);
		this.callback.onEnd(obj);
		return initial;
	}

	private void walk(RecursionControl<Object, T> ctrl, Object obj) {
		callback.onBeforeRecursion(obj);
		if (isLeaf(obj)) {
			callback.onJavaValue(obj);
		} else if (isMap(obj)) {
			if (ctrl.doRecursion(obj)) {
				callback.onBeforeJavaMap((Map<?, ?>) obj);
				for (Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
					if (isLeaf(e.getValue()))
						callback.onJavaMapValue((Map<?, ?>) obj, e);
					else if (null != e.getValue()) {
						callback.onBeforeRecursion(e.getValue());
						walk(ctrl, e.getValue());

					}
				}
				callback.onAfterJavaMap((Map<?, ?>) obj);
			}
		} else if (isArray(obj)) {
			if (ctrl.doRecursion(obj)) {
				if (obj instanceof Iterable) {
					callback.onBeforeJavaArray((Iterable<?>) obj);
					int idx = 0;
					for (Object i : (Iterable<?>) obj) {
						callback.onBeforeJavaArrayItem(i,idx);
						callback.onJavaArrayItem( i, idx);
						walk(ctrl,i);
						callback.onAfterJavaArrayItem(i,idx);
						++idx;
					}
					callback.onAfterJavaArray((Iterable<?>) obj);
				} else if (obj instanceof Object[]) {
					callback.onBeforeJavaArray((Object[]) obj);
					int idx = 0;
					for (Object i : (Object[]) obj) {
						callback.onBeforeJavaArrayItem(i,idx);
						callback.onJavaArrayItem( i, idx);
						walk(ctrl,i);
						callback.onAfterJavaArrayItem(i,idx);
						++idx;
					}
					callback.onAfterJavaArray((Object[]) obj);
				}
			}
		} else if (null != obj) {
			if (ctrl.doRecursion(obj)) {
				callback.onBeforeJavaObject(obj);
				ObjectIterator it = new ObjectIterator(obj);

				for (Entry<String, Object> e : it.map().entrySet()) {
					if (isLeaf(e.getValue()))
						callback.onJavaObjectValue(obj, e);
					else if (null != e.getValue()) {
						walk(ctrl, e.getValue());
					}
				}
				callback.onAfterJavaObject(obj);
			}
		}
		callback.onAfterRecursion(obj);
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
