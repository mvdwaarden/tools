package json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import data.LogUtil;
import object.ObjectFactory;
import object.ObjectIterator;

public class JavaWriter {

	public Object write(JSONObject jsonObject, Object root) {

		JavaWriterCallback cb = new JavaWriterCallback();
		cb.root = root;

		JSONWalker walker = new JSONWalker();

		walker.walk(jsonObject, cb);

		return root;
	}

	class JavaWriterCallback extends JSONWalkerCallbackAdapter {
		Stack<Object> stack = new Stack<>();
		Stack<ParameterizedType> listTypeStack = new Stack<>();
		Map<Field, Object> fields;
		Class<?> clsList;
		Object root;

		void setFields() {
			if (stack.isEmpty()) {
				ObjectIterator it = new ObjectIterator(root);
				fields = it.map(Field.class);
			} else if (null != stack.peek()) {
				ObjectIterator it = new ObjectIterator(stack.peek());
				fields = it.map(Field.class);
			}
		}

		@Override
		public void onBeginRecursion(String tag, JSONObject parent, JSONObject object) {
			setFields();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onBeforeList(String tag, JSONList list) {
			// special case, first time add the root
			if (stack.isEmpty()) {
				stack.push(root);
			} else {
				Field field = getField(tag);
				if (null != field) {
					addObject(field);
					ParameterizedType type = (ParameterizedType) field.getGenericType();
					listTypeStack.push(type);
				} else {
					listTypeStack.push(null);
					stack.push(new ArrayList());
				}
			}
		}

		@Override
		public void onAfterList(String tag, JSONList list) {
			stack.pop();
			listTypeStack.pop();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void onBeforeRecord(String tag, JSONRecord record) {
			// special case, first time add the root
			if (stack.isEmpty()) {
				stack.push(root);
			} else {
				Field field = getField(tag);
				if (null != field) {
					addObject(field);
				} else if (stack.peek() instanceof List) {
					Object obj = createListItemObject();
					((List) stack.peek()).add(obj);
					stack.push(obj);
				} else {
					stack.push(null);
				}
			}
		}

		@Override
		public void onAfterRecord(String tag, JSONRecord record) {
			stack.pop();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void onValue(String tag, json.JSONValue<?> value) {
			Field field = getField(tag);

			if (null != field) {
				if (stack.peek() instanceof List)
					((List) stack.peek()).add(value.getData());
				else if (null != stack.peek())
					try {
						if (value.getData() instanceof Long && field.getType() == Integer.class
								|| field.getType() == int.class)
							field.set(stack.peek(), ((Long) value.getData()).intValue());
						else if (value.getData() instanceof String && field.getType() == Boolean.class
								|| field.getType() == boolean.class)
							field.set(stack.peek(), Boolean.parseBoolean((String) value.getData()));
						else if (value.getData() instanceof String && ((String) value.getData()).matches("....-..-..")
								&& field.getType() == Date.class) {
							SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
							field.set(stack.peek(), fmt.parse((String) value.getData()));
						} else
							field.set(stack.peek(), value.getData());
					} catch (IllegalArgumentException | IllegalAccessException | ParseException e) {
						LogUtil.getInstance().warning("problem setting object value", e);
					}
			}
		}

		private Field getField(String tag) {
			Field result = null;
			if (null != fields) {
				Optional<Field> opt = fields.keySet().stream().filter(f -> f.getName().equals(tag)).findFirst();

				if (opt.isPresent())
					result = opt.get();
			}
			return result;
		}

		public Object createListItemObject() {
			Object result = null;

			if (stack.peek() instanceof List && null != listTypeStack.peek()) {
				Class<?> cls = (Class<?>) listTypeStack.peek().getActualTypeArguments()[0];

				result = ObjectFactory.getInstance().createObject(cls);
			}
			return result;
		}

		@SuppressWarnings("rawtypes")
		public Object createObject(Field field) {
			Object result = null;
			if (null != field) {
				// lists always need to be initialized by the object itself
				if (field.getType().isAssignableFrom(List.class)) {
					try {
						// check if constructor created list
						result = field.get(stack.peek());
						if (null == result) {
							// try invoke getter to initialize the list
							Method m = field.getDeclaringClass().getMethod("get"
									+ field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
									new Class[] {});
							m.setAccessible(true);
							result = (List<?>) m.invoke(stack.peek());
						}
						if (null == result) {
							result = new ArrayList();
						}
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException
							| SecurityException | InvocationTargetException e) {
						LogUtil.getInstance().warning("unable to get list member", e);
					}
				} else {
					result = ObjectFactory.getInstance().createObject(field.getType());
				}
			}
			return result;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void addObject(Field field) {
			Object obj = createObject(field);
			try {
				if (stack.peek() instanceof List)
					((List) stack.peek()).add(obj);
				else if (null != stack.peek())
					field.set(stack.peek(), obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogUtil.getInstance().warning("problem setting object value", e);
			}
			stack.push(obj);
		}
	}

}
