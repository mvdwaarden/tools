package algoritm;

import java.util.HashMap;
import java.util.Map;

public class RecursionControl<K, V> {
	private Map<K, V> recursion = new HashMap<>();
	private V dummy;

	public RecursionControl(V dummy) {
		this.dummy = dummy;
	}

	public boolean doRecursion(K obj) {
		boolean result = false;
		if (null == recursion.get(obj)) {
			recursion.put(obj, dummy);
			result = true;
		}
		return result;
	}

	public void put(K obj, V jsonObject) {
		recursion.put(obj, jsonObject);
	}

	public V get(K obj) {
		return recursion.get(obj);
	}
}
