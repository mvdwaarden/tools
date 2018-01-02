package xml;

/**
 * 
 * @author mwa17610
 *
 *         Purpose allows for execution of custom test methods
 * 
 * @see ExecutionContext#custom(String, CustomMethod)
 */
public interface CustomMethod {
	/**
	 * 
	 * @return true if the method is executed correctly.
	 */
	boolean isOk();

	/**
	 * Custom method execution method.
	 * 
	 * @param ctx
	 *            execution context
	 * @return the execution context passed in ctx.
	 */
	ExecutionContext execute(ExecutionContext ctx);

	/**
	 * @return the execution context passed in the
	 *         {@link #execute(XsltExecutionContext)} method.
	 */
	ExecutionContext getExecutionContext();
}
