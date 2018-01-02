package object;

import java.lang.reflect.Constructor;

/**
 * Purpose: object creation factory
 * 
 * @author mwa17610
 * 
 */
public class ObjectFactory {
	private static final ThreadLocal<ObjectFactory> factory = new ThreadLocal<ObjectFactory>();

	private ObjectFactory() {

	}

	/**
	 * Singleton instance method
	 * 
	 * @return
	 */
	public static ObjectFactory getInstance() {
		ObjectFactory result = factory.get();
		if (null == result) {
			result = new ObjectFactory();
			factory.set(result);
		}
		return result;
	}

	/**
	 * From the list of constructor for a Java class, return the constructor at
	 * the specific index
	 * 
	 * @param cls
	 * @param idx
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public <T> Constructor<T> getConstructor(Class<T> cls, int idx) {
		Constructor<T> result = null;
		try {
			result = (Constructor<T>) cls.getConstructors()[idx];
		} catch (Exception e) {
			StringBuilder str = new StringBuilder("Constructor not found [" + cls.getName() + "]");
			throw new RuntimeException(str.toString(), e);
		}

		return result;
	}

	/**
	 * Get a constructor for a Java object, based on the class and the parameter
	 * types
	 * 
	 * @param cls
	 * @param paramTypes
	 * @return
	 */
	public <T> Constructor<T> getConstructor(Class<T> cls, Class<?>... paramTypes) {
		Constructor<T> result = null;
		try {
			result = cls.getConstructor(paramTypes);
		} catch (NoSuchMethodException e) {
			StringBuilder str = new StringBuilder("Constructor not found [" + cls.getName() + "(");
			for (Class<?> clsParam : paramTypes) {
				str.append(clsParam.getName());
				str.append(" ");
			}
			str.append(")]");
			throw new RuntimeException(str.toString(), e);
		}

		return result;
	}

	/**
	 * Create an object based on the full qualified class name (fqn)
	 * 
	 * @param fqn
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T createObject(String fqn) {
		T result = null;

		try {
			Class<?> cls = Class.forName(fqn);
			result = createObject((Class<T>) cls, new Class[] {}, new Object[] {});
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Can not find class for [" + fqn + "]", e);
		}

		return result;
	}

	/**
	 * Create a Java object based on the full qualified class name (fqn),
	 * specific constructor at specific index.
	 * 
	 * @param fqn
	 * @param idx
	 * @param parameters
	 * @return
	 */
	public <T> T createObject(String fqn, int idx, Object... parameters) {
		T result = null;

		try {
			Class<?> cls = Class.forName(fqn);
			@SuppressWarnings("unchecked")
			Constructor<T> ctor = getConstructor((Class<T>) cls, idx);
			result = createObject(ctor, parameters);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Can not find class for [" + fqn + "]", e);
		}

		return result;
	}

	/**
	 * Create a Java object based on full qualified class name (fqn) and the
	 * parameter types
	 * 
	 * @param fqn
	 * @param paramTypes
	 * @param parameters
	 * @return
	 */
	public <T> T createObject(String fqn, Class<?>[] paramTypes, Object... parameters) {
		T result = null;

		try {
			Class<?> cls = Class.forName(fqn);
			@SuppressWarnings("unchecked")
			Constructor<T> ctor = getConstructor((Class<T>) cls, paramTypes);
			result = createObject(ctor, parameters);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Can not find class for [" + fqn + "]", e);
		}

		return result;
	}

	public <T> T createObject(Class<T> cls) {
		return createObject(cls, new Class[] {}, new Object[] {});
	}

	public <T> T createObject(Class<T> cls, Object... parameters) {
		Class<?>[] paramTypes = new Class[parameters.length];

		for (int i = 0; i < parameters.length; ++i) {
			paramTypes[i] = parameters[i].getClass();
		}
		return createObject(cls, paramTypes, parameters);
	}

	public <T> T createObject(Class<T> cls, Class<?>[] paramTypes, Object... parameters) {
		T result = null;
		try {
			Constructor<T> ctor = getConstructor(cls, paramTypes);

			result = createObject(ctor, parameters);
		} catch (Exception e) {
			throw new RuntimeException("Unable to create object for [" + cls.getName() + "]", e);
		}

		return result;
	}

	public <T> T createObject(Constructor<T> ctor, Object... parameters) {
		T result = null;
		try {
			result = ctor.newInstance(parameters);
		} catch (Exception e) {
			throw new RuntimeException("Unable to create object for [" + ctor.getDeclaringClass().getName() + "]", e);
		}

		return result;
	}
}
