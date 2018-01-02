package object;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import data.EnumUtil;

/**
 * Purpose: Object iterator. Generic iteration over an objects member (the
 * reflection Field and the value).
 * 
 * The iterator can be used with: - Classes : iteration over fields - Objects :
 * iteration over fields and values
 * 
 * @author mwa17610
 * 
 */
@SuppressWarnings("rawtypes")
public class ObjectIterator implements Iterator {
	public enum Option {
		GETTER_SETTER;
	}

	/**
	 * The class used for iterator construction
	 */
	private Class<?> cls;
	/**
	 * The object used for iterator construction
	 */
	private Object object;
	/**
	 * internal index for the iterator
	 */
	private Iterator<Entry<String, Field>> iterator;
	/**
	 * field list
	 */
	private Map<String, Field> fields;
	/**
	 * Cache for fast iterator construction
	 */
	private final static Map<CacheKey, Map<String, Field>> fieldsLookup = new HashMap<>();

	/**
	 * Cache for fast iterator construction
	 */
	private final static Map<CacheKey, Map<String, Method>> methodsLookup = new HashMap<>();

	private ObjectIterator(ObjectIterator it, Field[] fields) {
		init(it, fields);
	}

	public ObjectIterator(Class<?> baseClass, Class<?> cls) {
		init(baseClass, cls);
	}

	public ObjectIterator(Class<?> cls, Option... options) {
		if (EnumUtil.getInstance().contains(options, Option.GETTER_SETTER))
			init(cls, _GetterSetterBase.class);
		else
			init(cls, cls);
	}

	public ObjectIterator(Object object) {
		if (null != object) {
			init(object.getClass(), object);
		}
	}

	public ObjectIterator(Object object, Class<?> baseClass) {
		if (null != object) {
			init((null == baseClass) ? object.getClass() : baseClass, object);
		}
	}

	private void init(ObjectIterator it, Field[] fields) {
		this.object = it.object;
		this.cls = it.cls;
		this.fields = new HashMap<>();
		for (Field f : fields)
			this.fields.put(f.getName(), f);
	}

	private void init(Class<?> baseClass, Class<?> cls) {
		this.cls = cls;
		this.object = null;
		this.fields = lookupFields(baseClass, cls);
	}

	private void init(Class<?> baseClass, Object object) {
		this.object = object;
		if (null != object) {
			this.cls = object.getClass();
			this.fields = lookupFields(baseClass, object.getClass());
		}

	}

	public ObjectIterator(Class<?> baseClass, Object object) {
		init(baseClass, object);
	}

	/**
	 * Filter members which are derived from a certain base class, filtering is
	 * done on the Field NOT the value!
	 * 
	 * @param cls
	 *            base class
	 * @return
	 */
	public ObjectIterator filter(Class<?> cls) {
		Field[] result = null;
		Field[] tmp = new Field[fields.size()];

		// ///////////////////////////////////////
		// filter only assignable fields
		// ///////////////////////////////////////
		int idx = 0;
		for (Field f : fields.values()) {
			if (cls.isAssignableFrom(f.getType())) {
				tmp[idx++] = f;
			}
		}
		if (idx > 0) {
			result = new Field[idx];

			System.arraycopy(tmp, 0, result, 0, idx);

		} else {
			result = new Field[0];
		}
		return new ObjectIterator(this, result);
	}

	/**
	 * Returns a map from the object fieldnames and the values
	 * 
	 * @return
	 */
	public Map<String, Object> map() {
		return map(String.class);
	}

	/**
	 * Returns a map which contains either the fieldname as a string OR the
	 * field itself as the value
	 * 
	 * @param cls
	 *            Either String.class or Field.class.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<T, Object> map(Class<T> cls) {
		Map<T, Object> result = new HashMap<>();

		for (Field f : fields.values()) {
			Object key = null;
			Object value = null;
			// do not include reference to self, for inner classes
			if (!f.getName().startsWith("this$")) {
				if (null != object) {
					try {
						value = f.get(object);
						if (cls == Field.class) {
							key = f;
						} else if (cls == String.class) {
							key = f.getName();
						}
					} catch (IllegalAccessException | IllegalArgumentException e) {
						throw new RuntimeException(e);
					}
				}
				result.put((T) key, value);
			}
		}

		return result;

	}

	/**
	 * Look up the fields for a class
	 * 
	 * @param cls
	 * @return
	 */
	public static Map<String, Field> lookupFields(Class<?> baseClass, Class<?> cls) {
		return lookup(baseClass, cls, fieldsLookup, c -> c.getDeclaredFields(), f -> {
			f.setAccessible(true);
			return f.getName();
		});
	}

	/**
	 * Look up the methods for a class
	 * 
	 * @param cls
	 * @return
	 */
	public static Map<String, Method> lookupMethods(Class<?> baseClass, Class<?> cls) {
		return lookup(baseClass, cls, methodsLookup, c -> c.getDeclaredMethods(), t -> t.getName());
	}

	/**
	 * Generic lookup method for a class
	 * 
	 * @param cls
	 * @return
	 */
	protected static <T> Map<String, T> lookup(Class<?> baseClass, Class<?> cls, Map<CacheKey, Map<String, T>> cache,
			Function<Class, T[]> map, Function<T, String> getName) {
		CacheKey key = new CacheKey(cls, baseClass);
		Map<String, T> result = cache.get(key);
		if (null == result) {
			result = new HashMap<>();
			for (T t : map.apply(cls))
				result.put(getName.apply(t), t);
			// /////////////////////////////////////
			// also add the superclass
			// ////////////////////////////////////
			boolean checkSuperClass = true;
			Class<?> tmpCls = cls;
			while (checkSuperClass) {
				tmpCls = tmpCls.getSuperclass();
				if (baseClass.isAssignableFrom(tmpCls)) {
					for (T t : map.apply(tmpCls))
						result.put(getName.apply(t), t);
				} else {
					checkSuperClass = false;
				}
			}

			cache.put(key, result);
		}

		return result;
	}

	/**
	 * Check if there is a next value. TRUE : yes, FALSE : no
	 */
	@Override
	public boolean hasNext() {
		if (null == iterator)
			iterator = fields.entrySet().iterator();
		return iterator.hasNext();
	}

	/**
	 * Return the next object. Object must be provided.
	 */
	@Override
	public Object next() {
		Object result = null;

		try {
			result = iterator.next().getValue().get(object);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Return the next field.
	 * 
	 * @return
	 */
	public Field nextField() {
		return iterator.next().getValue();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported on ObjectTreeIterator");
	}

	/**
	 * Convenience, position iterator to first.
	 */
	public void moveFirst() {
		iterator = fields.entrySet().iterator();
	}

	private class _GetterSetterBase {

	}

	public static class CacheKey {
		Class object;
		Class base;
		String key;

		public CacheKey(Class object) {
			this(object, _GetterSetterBase.class);
		}

		public CacheKey(Class object, Class base) {
			this.object = object;
			this.base = base;
			key = object.getName() + ":" + base.getName();
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if (obj instanceof CacheKey)
				result = (((CacheKey) obj).object == object && ((CacheKey) obj).base == base);
			else
				result = false;

			return result;
		}
	}
}
