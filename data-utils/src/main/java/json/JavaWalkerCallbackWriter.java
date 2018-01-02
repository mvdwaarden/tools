package json;

import java.util.Map;
import java.util.Map.Entry;

import data.LogUtil;

public class JavaWalkerCallbackWriter<T> implements JavaWalkerCallback<T> {

	@Override
	public void onStart(Object obj, Object initial) {
		LogUtil.getInstance().info("onStart");

	}

	@Override
	public void onEnd(Object obj) {
		LogUtil.getInstance().info("onEnd");

	}

	@Override
	public void onJavaValue(Object obj) {
		LogUtil.getInstance().info("onLeaf(" + obj.toString() + ")");
	}

	@Override
	public void onBeforeJavaMap(Map<?, ?> obj) {
		LogUtil.getInstance().info("onBeforeJavaMap");

	}

	@Override
	public void onJavaMapValue(Map<?, ?> obj, Entry<?, ?> e) {
		LogUtil.getInstance().info("onJavaMapValue (" + ((null != e.getKey()) ? e.getKey().toString() : "") + ","
				+ ((null != e.getValue()) ? e.getValue().toString() : "") + ")");
	}

	@Override
	public void onAfterJavaMap(Map<?, ?> obj) {
		LogUtil.getInstance().info("onAfterJavaMap");
	}

	@Override
	public void onBeforeRecursion(Object value) {
		LogUtil.getInstance().info("onBeforeRecursion");

	}

	@Override
	public void onAfterRecursion(Object value) {
		LogUtil.getInstance().info("onAfterRecursion");

	}

	@Override
	public void onBeforeJavaArray(Iterable<?> obj) {
		LogUtil.getInstance().info("onBeforeJavaArray");
	}

	@Override
	public void onAfterJavaArray(Iterable<?> obj) {
		LogUtil.getInstance().info("onAfterJavaArray");

	}

	@Override
	public void onBeforeJavaArray(Object[] obj) {
		LogUtil.getInstance().info("onBeforeJavaArray[]");

	}

	@Override
	public void onAfterJavaArray(Object[] obj) {
		LogUtil.getInstance().info("onAfterJavaArray[]");

	}

	@Override
	public void onBeforeJavaObject(Object obj) {
		LogUtil.getInstance().info("onBeforeJavaObject");

	}

	@Override
	public void onJavaObjectValue(Object obj, Entry<?, ?> e) {
		LogUtil.getInstance().info("onJavaObjectValue(" + ((null != e.getKey()) ? e.getKey().toString() : "") + ","
				+ ((null != e.getValue()) ? e.getValue().toString() : "") + ")");

	}

	@Override
	public void onAfterJavaObject(Object obj) {
		LogUtil.getInstance().info("onAfterJavaObject");

	}

	@Override
	public void onBeforeJavaArrayItem(Object i, int idx) {
		LogUtil.getInstance().info("beforeJavaArrayItem");

	}

	@Override
	public void onJavaArrayItem(Object i, int idx) {
		LogUtil.getInstance().info("onJavaArrayItem[" + idx + "]");

	}

	@Override
	public void onAfterJavaArrayItem(Object i, int idx) {
		LogUtil.getInstance().info("onAfterJavaArrayItem");

	}

}
