package data;

public class EnumUtil implements Util {
	private static final ThreadLocal<EnumUtil> instance = new ThreadLocal<EnumUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static EnumUtil getInstance() {
		EnumUtil result = instance.get();

		if (null == result) {
			result = new EnumUtil();
			instance.set(result);
		}

		return result;
	}

	/**
	 * 
	 * @param clsEnum
	 * @param en
	 * @return
	 */
	public <T extends Enum<T>> boolean contains(Enum<T>[] enums, Enum<T> en) {
		boolean result = false;

		for (Enum<T> e : enums)
			if (en == e) {
				result = true;
				break;
			}

		return result;
	}

	/**
	 * 
	 * @param enumType
	 * @param name
	 * @return
	 */
	public <T extends Enum<T>> Enum<T> getByName(Class<T> enumType, String name) {
		return Enum.valueOf(enumType, name);
	}
}
