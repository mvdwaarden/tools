package object;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import data.EnumUtil;
import data.LogUtil;

public class ObjectUtil {
	public enum Option {
		INTEGER, FLOATING_POINT
	}

	private static final ThreadLocal<ObjectUtil> instance = new ThreadLocal<ObjectUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static ObjectUtil getInstance() {
		ObjectUtil result = instance.get();

		if (null == result) {
			result = new ObjectUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * Filter a list so that it contains only a specific subtype
	 * 
	 * @param items
	 * @param clsSubtype
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T, R extends T> List<R> filter(List<T> items, Class<R> clsSubtype) {
		List<R> result = (List<R>) items.stream()
				.filter(item -> null != item && clsSubtype.isAssignableFrom(item.getClass()))
				.collect(Collectors.toList());

		return result;
	}

	/**
	 * Filter a list so that it contains only specific subtypes
	 * 
	 * @param items
	 * @param clsSubtype
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T, R extends T> List<R> filter(List<T> items, Class<R>[] subtypeClasses) {
		List<R> result = (List<R>) items.stream().filter(item -> {
			boolean add = false;
			for (Class<R> clsSubtype : subtypeClasses) {
				if (null != item && clsSubtype.isAssignableFrom(item.getClass())) {
					add = true;
					break;
				}
			}
			return add;
		}).collect(Collectors.toList());

		return result;
	}

	public Map<String, Object> map(Object obj) {
		ObjectIterator it = new ObjectIterator(obj);

		return it.map();
	}

	public Map<String, Object> map(Object obj, Class<?> baseClass) {
		ObjectIterator it = new ObjectIterator(obj, baseClass);

		return it.map();
	}

	/**
	 * Verify if this object is of a certain type.
	 * 
	 * @param obj
	 * @param classes
	 * @param includeSubClasses
	 * @return
	 */
	public boolean isA(Object obj, Class<?>[] classes, boolean includeSubClasses) {
		boolean result = false;
		if (null != obj) {
			for (Class<?> cls : classes) {
				if ((!includeSubClasses && cls.equals(obj.getClass()))
						|| (includeSubClasses && cls.isAssignableFrom(obj.getClass()))) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Test if the object is a primitive
	 * 
	 * @return
	 */
	public boolean isPrimitive(Object obj, Option... options) {
		boolean result = false;
		if (null != obj)
			result = isNumber(obj, options) || isString(obj) || isDate(obj) || isBoolean(obj);
		return result;

	}

	/**
	 * Test if an object is a number
	 * 
	 * @param obj
	 * @return
	 */
	public boolean isNumber(Object obj, Option... options) {
		if (null != obj)
			return obj instanceof Byte || obj.getClass() == byte.class || obj instanceof Character
					|| obj.getClass() == char.class || obj instanceof Short || obj.getClass() == short.class
					|| obj instanceof Integer || obj.getClass() == int.class || obj instanceof Long
					|| obj.getClass() == long.class
					|| (EnumUtil.getInstance().contains(options, Option.FLOATING_POINT)
							&& (obj instanceof Float || obj.getClass() == float.class || obj instanceof Double
									|| obj.getClass() == double.class))
					|| (EnumUtil.getInstance().contains(options, Option.FLOATING_POINT) && obj instanceof BigInteger);
		else
			return false;
	}

	/**
	 * Test if an object is a number
	 * 
	 * @param obj
	 * @return
	 */
	public boolean isBoolean(Object obj) {
		if (null != obj)
			return obj instanceof Boolean || obj.getClass() == boolean.class;
		else
			return false;
	}

	/**
	 * Test is an object is a date
	 */
	public boolean isDate(Object obj) {
		return obj instanceof Date;
	}

	/**
	 * Test is an object is a string
	 */
	public boolean isString(Object obj) {
		return obj instanceof String;
	}

	/**
	 * Merges object 1 into object 2. This means that if a value for object 2 is
	 * NULL that the value from object 1 is taken.
	 * 
	 * The operation is NOT commutative
	 */
	public <T> T merge(T obj1, T obj2) {
		return merge(obj1, obj2, null);
	}

	/**
	 * Merges object 1 into object 2. This means that if a value for object 2 is
	 * NULL that the value from object 1 is taken.
	 * 
	 * The operation is NOT commutative
	 */
	public <T> T merge(T obj1, T obj2, Class<?> baseClass) {
		ObjectIterator it1 = new ObjectIterator(obj1, baseClass);
		ObjectIterator it2 = new ObjectIterator(obj2, baseClass);

		Map<Field, Object> fields1 = it1.map(Field.class);
		Map<Field, Object> fields2 = it2.map(Field.class);

		for (Entry<Field, Object> e : fields1.entrySet()) {
			if (null != e.getValue()) {
				// find field in object 2
				Optional<Entry<Field, Object>> opt = fields2.entrySet().stream()
						.filter(t -> t.getKey().getName().equals(e.getKey().getName())).findFirst();
				if (opt.isPresent()) {
					Object value;
					try {
						value = opt.get().getKey().get(obj2);
						if (null == value)
							opt.get().getKey().set(obj2, e.getValue());
					} catch (IllegalArgumentException | IllegalAccessException ex) {
						LogUtil.getInstance().error("merge problem", ex);
					}
				}
			}
		}

		return obj2;
	}
	
	/**
	 * Invokes a method on an object.
	 *
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T invoke(Object obj, String methodName, Class<?>[] paramTypes, Object[] params) {
		T result = null;
		if (null != obj) {			
			Class<?> cls = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();
			Method method = null;
			try {
				method = cls.getMethod(methodName, paramTypes);
				result = (T) method.invoke((obj instanceof Class) ? null : obj, params);
			} catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException
					| InvocationTargetException e) {
				StringBuilder str = new StringBuilder("Problem invoking [" + cls.getName() + ":" + methodName + "(");
				for (Class<?> clsParam : paramTypes) {
					str.append(clsParam.getName());
					str.append(" ");
				}
				str.append(")]");
				throw new RuntimeException(str.toString(), e);
			}
		}
		return result;
	}

	public void setValue(Object result, Field field, String value) {
		try {
			if (field.getType() == String.class)
				field.set(result, value);
			else if (field.getType() == Integer.class || field.getType() == int.class)
				field.setInt(result, Integer.parseInt(value));
			else if (field.getType() == Long.class || field.getType() == long.class)
				field.setLong(result, Long.parseLong(value));
			else if (field.getType() == Double.class || field.getType() == double.class)
				field.setDouble(result, Double.parseDouble(value));
			else if (field.getType() == Float.class || field.getType() == float.class)
				field.setDouble(result, Float.parseFloat(value));
			else if (field.getType() == Boolean.class || field.getType() == boolean.class)
				field.setBoolean(result, Boolean.parseBoolean(value));
			else if (field.getType() == Byte.class || field.getType() == byte.class)
				field.setByte(result, Byte.parseByte(value));
			else if (field.getType() == Short.class || field.getType() == short.class)
				field.setShort(result, Short.parseShort(value));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LogUtil.getInstance().warning("unable to set value [" + field.getName() + "] to [" + value + "]");
		}

	}

	/**
	 * Converts an object in to a hashmap of key value pairs
	 * 
	 * Referenced objects are NOT converted
	 * 
	 * @param node
	 * @param baseClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> object2NV(Object node, Class baseClass, Predicate<Object> validValue) {

		ObjectIterator itObj = new ObjectIterator(baseClass, node);

		Map<String, Object> result = itObj.map();

		Iterator<Entry<String, Object>> it = result.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> e = it.next();
			if (!validValue.test(e.getValue()))
				it.remove();
		}

		return result;
	}
}
