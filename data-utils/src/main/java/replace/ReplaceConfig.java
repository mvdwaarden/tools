package replace;

import java.io.OutputStream;

import org.xml.sax.Attributes;

/**
 * Interface for a replacement lambda function
 * 
 * @author mwa17610
 *
 */
public interface ReplaceConfig {
	/**
	 * Check and replace.
	 * 
	 * <pre>
	 * - Input: external replacement context and XML tag information. 
	 * - Output: an output stream.
	 * </pre>
	 * 
	 * @param ctx
	 *            external replacement context
	 * @param tagUri
	 * @param tagLocalName
	 * @param tagQName
	 * @param tagAtts
	 * @param os
	 * @return true if replacement is performed (all subsequent tags are
	 *         skipped), false otherwise.
	 */
	boolean checkReplace(ReplaceContext ctx, String tagUri, String tagLocalName, String tagQName, Attributes tagQtts,
			OutputStream os, String[] files);
}
