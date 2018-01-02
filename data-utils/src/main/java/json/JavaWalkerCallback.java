package json;

import java.util.Map;
import java.util.Map.Entry;

public interface JavaWalkerCallback<T> {
	void onStart(Object obj, T initial);

	void onEnd(Object obj);

	void onJavaValue(Object obj);

	void onBeforeJavaMap(Map<?, ?> obj);

	void onJavaMapValue(Map<?, ?> obj, Entry<?, ?> e);

	void onAfterJavaMap(Map<?, ?> obj);

	void onBeforeRecursion(Object value);

	void onAfterRecursion(Object value);

	void onBeforeJavaArray(Iterable<?> obj);

	void onAfterJavaArray(Iterable<?> obj);

	void onBeforeJavaArray(Object[] obj);

	void onAfterJavaArray(Object[] obj);

	void onBeforeJavaObject(Object obj);

	void onJavaObjectValue(Object obj, Entry<?, ?> e);

	void onAfterJavaObject(Object obj);

	void onBeforeJavaArrayItem(Object i, int idx);

	void onJavaArrayItem(Object i, int idx);

	void onAfterJavaArrayItem(Object i, int idx);
}
