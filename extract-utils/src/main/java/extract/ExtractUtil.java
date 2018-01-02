package extract	;

/**
 * Purpose: Utility class for graph package
 * 
 * @author mwa17610
 * 
 */
public class ExtractUtil {
	private static final ThreadLocal<ExtractUtil> instance = new ThreadLocal<ExtractUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static ExtractUtil getInstance() {
		ExtractUtil result = instance.get();

		if (null == result) {
			result = new ExtractUtil();
			instance.set(result);
		}

		return result;
	}
}
