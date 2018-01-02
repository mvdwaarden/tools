/* ===============================================================================
 * Project:  Utility
 * Subproject: Data Utilities
 * Copyright:  (C) Ordina ICT B.V.
 * Original Author : Mando van der Waarden
 * Creation date : 01-01-2015
 * ===============================================================================
 */
package xml;

import java.io.File;
import java.util.List;

import data.Filter;

/**
 * <pre>
 * Purpose : XML Execution Context.
 * 
 * It allows for execution chaining on the following methods:
 * 1) load
 * 2) transform
 * 3) validate
 * 4) assertTrue
 * 5) copy
 * 6) custom
 * 7) registerNamespace
 * 8) registerNamespaces
 * 9) unregisterNamespace
 * 
 * Consecutive operations will only perform if a previous operation has succeeded.
 * 
 * Example 1 :  (load, transform, copy, validate, assertTrue). The getFilename() is a convenience method.
 * <code>
 * 		XmlExecutionContext ctx = new XsltTestContext();
 * 
 * 		ctx.load("step_1_load", getFilename(filenames, "INPUT"))
 * 				.transform("step_2_transform",
 * 						getFilename(filenames, "TRANSFORMATION_1"))
 * 				.copy("step_3_copy",
 * 						getFilename(filenames, "TRANSFORMATION_RESULT_1"))
 * 				.validate("step_4_validate",
 * 						getFilename(filenames, "VALIDATION_1"))
 * 				.assertTrue(
 * 						"step_5_assert",
 * 						new String[][] {
 * 								{ "//ns1:whatever/ns2:value", "some value" },
 * 								{ "//ns1:whatever[ns1:tag1/text() = 'some value']/ns1:tag2/text()", "some value 2" },
 * 								{ "//ns1:whatever[ns1:tag3/text() = 'some value']/ns1:tag4/text()", "some value 3" }});
 * 		assertTrue(ctx.getFilename() + " ok", ctx.isOk());
 * 	</code>						
 * 						       
 * Version history: 
 * 30/03/2015 MWA creation
 * </pre>
 * 
 * @author mwa17610
 * @since 30/03/2015
 */
public interface ExecutionContext {
	/**
	 * Use the constant to specify the last result in a transformation with
	 * multiple bindings
	 */
	public static final String LAST_RESULT = "$LAST_RESULT";

	/**
	 * Reset the status of the execution pipeline.
	 */
	ExecutionContext reset();

	/**
	 * substitutes values in an XML according to replaceConfig (see StringUtil)
	 * 
	 * @param step
	 *            name for the current operation
	 * @param replaceConfig
	 *            StringUtil::replace()
	 * 
	 * @return the execution context (this)
	 */
	public ExecutionContext subst(String step, String replaceConfig);

	/**
	 * Transform the current input using an XSLT
	 * 
	 * @param step
	 *            name for the current operation
	 * @param script
	 *            the filename of the script to use for the transformation
	 * @param type
	 *            script type (XSLT or XQuery)
	 * @return the execution context (this)
	 */
	ExecutionContext transform(String step, String script, XMLTransformer.ScriptType type);

	/**
	 * Transform the current input using binding information. Use LAST_RESULT
	 * constant to specify the location of the current input
	 * 
	 * @param step
	 *            name for the current operation
	 * @param binder
	 *            the binder to use
	 * @param script
	 *            the filename of the script to use for the transformation
	 * @param type
	 *            script type (XSLT or XQuery)
	 * @return the execution context (this)
	 */
	public ExecutionContext transform(String step, XMLBinder binder, String script, XMLTransformer.ScriptType type);

	/**
	 * Validate the current input using an XSD
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param xsdFilename
	 *            filename of the XSD to validate against
	 * @return the execution context (this)
	 */
	ExecutionContext validate(String step, String xsdFilename);

	/**
	 * Load a file. The file will become the input. Typically this is done at
	 * the beginning of a chain.
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param sourceFilename
	 *            the filename to load
	 * @return the execution context (this)
	 */
	ExecutionContext load(String step, String sourceFilename);

	/**
	 * Load a directory.
	 * 
	 * The result is an XML that is loaded in the context.
	 * 
	 * The result conforms to http://xml/ExecutionContext
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param dirname
	 *            the directory to load
	 * @param filter
	 *            filter to use
	 * 
	 * @return the execution context (this)
	 */
	ExecutionContext load(String step, String dirname, Filter<File> filter);

	/**
	 * Copy the current input to a specific file
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param targetFilename
	 *            filename for the copy of the input
	 * @return the execution context (this)
	 */
	ExecutionContext copy(String step, String targetFilename);

	/**
	 * Assert the input based on an XPath expression. The result of the XPath
	 * expression is compared to the value.
	 * 
	 * If the result of the XPath and the value do not match, a report entry is
	 * written.
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param xpath
	 *            XPath expression
	 * @param value
	 *            value to compare the result of the xpath against
	 * @return the execution context (this)
	 */
	ExecutionContext assertTrue(String step, String xpath, String value);

	/**
	 * Assert the input based on multiple XPath expression. ALL assertions are
	 * executed.
	 * 
	 * If the result of a XPath and the value do not match, a report entry is
	 * written.
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param assertions
	 *            two dimensional array to contain the assertions. [][0]
	 *            contains the XPath expression. [][1] contains the expected
	 *            value.
	 * @return the execution context (this)
	 */
	ExecutionContext assertTrue(String step, String[][] assertions);

	/**
	 * Execute a custom method
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param custom
	 *            custom method implementation
	 * @return the execution context (this)
	 */
	ExecutionContext custom(String step, CustomMethod custom);

	/**
	 * Register a namespace prefix. The namespace prefix can by used in XPath
	 * expressions. Existing namespace prefixes will be overwritten. * @param
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param prefix
	 *            namespace prefix
	 * @param uri
	 *            namespace URI
	 * @return the execution context (this)
	 */
	ExecutionContext registerNamespace(String step, String prefix, String uri);

	/**
	 * Unregister a namespace prefix. The namespace prefix will be removed and
	 * can no longer be used in XPath expressions.
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param prefix
	 *            prefix to unregister (remove)
	 * @return the execution context (this)
	 */
	ExecutionContext unregisterNamespace(String step, String prefix);

	/**
	 * Register multiple namespace prefixes. The namespace prefixes can by used
	 * in XPath expressions.
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param namespaces
	 *            two dimensional string array. [][0] contains namespace prefix,
	 *            [][1] contains namespae URI
	 * @return the execution context (this)
	 */
	ExecutionContext registerNamespaces(String step, String[][] namespaces);

	/**
	 * @return the current status of the execution chain, i.e. true if all
	 *         operations are performed succesfully
	 */
	boolean isOk();

	/**
	 * @return the filename that corresponds with the current input
	 */
	String getFilename();

	/**
	 * Combines files in a folder into one big XML file
	 * 
	 * @param step
	 *            step name for the current operation
	 * @param sourcedir
	 *            folder for the source XML files
	 * @param regexFileFilter
	 *            regular expression for the file filter
	 * @param targetfile
	 *            target file
	 * @param namespace
	 *            namespace for the roottag
	 * @param prefix
	 *            prefix to use for the roottag
	 * @param roottag
	 *            the roottag
	 * 
	 * @return
	 */
	ExecutionContext combine(String step, String sourcedir, String regexFileFilter, String targetfile, String namespace,
			String prefix, String roottag);

	/**
	 * @return the name of the current step. At the end of execution chain it
	 *         will contains the name of the last correctly executed operation.
	 */
	String getStep();

	/**
	 * @return a list of failed assertions.
	 */
	List<String> getFailedAssertions();

}
