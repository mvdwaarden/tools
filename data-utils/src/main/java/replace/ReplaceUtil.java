package replace;

import java.io.OutputStream;
import java.util.Map;

import org.xml.sax.Attributes;

import xml.XMLSAXEchoHandler;
import xml.XMLUtil;

/**
 * Replace utitily.
 * 
 * @author mwa17610
 *
 */
public class ReplaceUtil {
	private static final ThreadLocal<ReplaceUtil> instance = new ThreadLocal<ReplaceUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static ReplaceUtil getInstance() {
		ReplaceUtil result = instance.get();

		if (null == result) {
			result = new ReplaceUtil();
			instance.set(result);
		}

		return result;
	}

	public void replace(String filename, String contextName, Object context, ReplaceConfig[] cfgs, OutputStream out) {
		replace(filename, new ReplaceContext(contextName, context), cfgs, out, new String[] {});
	}

	public void replace(String filename, Map<String, Object> context, ReplaceConfig[] cfgs, OutputStream out) {
		replace(filename, new ReplaceContext(context), cfgs, out, new String[] {});
	}

	/**
	 * <pre>
	 * Parses a XML file and replaces content. It allows the definition of a replacement context.
	 * At each startElement event:
	 *       try several replacements as specified by the replacement configurations
	 *       	if something IS replaced => skip all subsequent following  'childs' 
	 *       	otherwise try replace on childs (etc, recusive)
	 * </pre>
	 * 
	 * It uses an override of the {@link xml.XMLSAXEchoHandler}, which default
	 * behaviour is to copy the input to the output.
	 * 
	 * @param filename
	 * @param ctx
	 * @param cfgs
	 * @param out
	 * 
	 * @see xml.XMLSAXEchoHandler
	 * @see replace.ReplaceContext
	 */
	public void replace(String filename, ReplaceContext ctx, ReplaceConfig[] cfgs, OutputStream out, String[] files) {
		class ReplaceHandler extends XMLSAXEchoHandler {
			// marker which remembers at which level a replacement has been done
			public int replaceMark = -1;

			public ReplaceHandler(OutputStream out) {
				super(out);
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes atts) {
				superStartElement(uri, localName, qName, atts);
				if (!checkMarkReplace(depth)) {
					for (ReplaceConfig cfg : cfgs) {
						if (cfg.checkReplace(ctx, uri, localName, qName, atts, getOutputStream(), files))
							markReplace(depth - 1);
						else
							writeStartElement(uri, localName, qName, atts);
					}
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) {
				if (!checkMarkReplace(depth))
					writeEndElement(uri, localName, qName);
				else
					tryClearMarkReplace();
				superEndElement(uri, localName, qName);
			}

			public void markReplace(int depth) {
				replaceMark = depth;
			}

			public boolean checkMarkReplace(int depth) {
				return (depth > replaceMark && replaceMark > 0);
			}

			public void tryClearMarkReplace() {
				if (depth == replaceMark + 1)
					replaceMark = -1;
			}
		}
		XMLUtil.getInstance().parse(filename, new ReplaceHandler(out));
	}
}
