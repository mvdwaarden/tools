package xml;

/**
 * <pre>
 * Purpose : Report listener. Allows for custom reporting during XSLT unit tests.
 *         
 * Version history: 
 * 30/03/2015 MWA creation
 * </pre>
 * 
 * @author mwa17610
 * @since 30/03/2015
 */
public interface ReportListener {
	/**
	 * Called by XsltExecutionContext
	 * 
	 * @param cls
	 *            context class
	 * @param step
	 *            step from which the report method is called
	 * @param msg
	 *            the report message
	 */
	void report(Class<?> cls, String step, String msg);
}
