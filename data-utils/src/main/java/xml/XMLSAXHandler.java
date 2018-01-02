package xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import data.DataUtil;
import data.LogUtil;

public class XMLSAXHandler implements ContentHandler, LexicalHandler {
	public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XSI_TYPE = "type";
	private InputSource inputSource;
	protected XMLReader xmlReader;
	protected Map<String, Stack<String>> prefixes;
	protected boolean optUsePrefixMappingOverride;
	protected boolean optTrackPrefixMapping;
	protected boolean optTrackPath;
	protected boolean optClearDataOnElementStart;
	protected Stack<String> path;
	/**
	 * Optimization
	 */
	protected String currentPath;
	protected StringBuilder data;
	protected String sourceUrl;
	protected String baseUrl;
	protected String targetNamespace;
	protected int depth;

	public XMLSAXHandler() {
		super();
		this.prefixes = new HashMap<>();
		this.optUsePrefixMappingOverride = false;
		this.optTrackPrefixMapping = true;
		this.optTrackPath = true;
		this.optClearDataOnElementStart = true;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void startDocument() {
	}

	@Override
	public void endDocument() {
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) {
		if (optTrackPrefixMapping) {
			Stack<String> pstack = prefixes.get(prefix);

			if (null == pstack) {
				pstack = new Stack<>();
				prefixes.put(prefix, pstack);
			}
			pstack.push(uri);
		}
	}

	@Override
	public void endPrefixMapping(String prefix) {
		if (optTrackPrefixMapping) {
			Stack<String> pstack = prefixes.get(prefix);

			pstack.pop();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (null == data)
			data = new StringBuilder();

		data.append(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) {
	}

	@Override
	public void processingInstruction(String target, String data) {
	}

	@Override
	public void skippedEntity(String name) {
	}

	@SuppressWarnings("rawtypes")
	public boolean isOfClass(Object obj, Class[] classes) {
		boolean result = false;

		if (classes.length > 0 && null != obj) {
			for (Class cls : classes) {
				if (obj.getClass() == cls) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public String getPath() {
		if (null == currentPath && null != path) {
			currentPath = "";
			if (optTrackPath) {
				for (String pathEl : path)
					currentPath += DataUtil.PATH_SEPARATOR + pathEl;
			}
		}
		return currentPath;
	}

	public boolean pathTest(String pathTest) {
		boolean result;

		String path = getPath();
		if (pathTest.startsWith(DataUtil.PATH_SEPARATOR))
			result = path.equals(pathTest);
		else
			result = path.endsWith(pathTest);

		return result;
	}

	/**
	 * 
	 * @param qName
	 *            the qualified name provided by SAX
	 * @param uriTest
	 *            the URI to test
	 * @param localNameTest
	 *            the local name to test
	 * @param uri
	 *            the URI provided by SAX
	 * @param localName
	 *            the local name provided by SAX
	 * @return
	 */
	public boolean nodeTest(String qName, String uriTest, String localNameTest, String uri, String localName) {
		boolean result = false;

		if (optUsePrefixMappingOverride) {
			String[] parts = qName.split(":");

			if (parts.length == 2) {
				Stack<String> pstack = prefixes.get(parts[0]);

				if (!pstack.isEmpty() && isEqual(pstack.peek(), uriTest) && isEqual(localName, localNameTest))
					result = true;
			} else if (parts.length == 1 && uriTest == null && localNameTest.equals(localName)) {
				result = true;
			}
		} else if (isEqual(localNameTest, localName) && isEqual(uriTest, uri)) {
			result = true;
		}

		return result;
	}

	private boolean isEqual(String str1, String str2) {
		if (str1 == null && str2 == null)
			return true;
		if (str1 != null && str1.equals(str2))
			return true;
		if (str2 != null && str2.equals(str1))
			return true;

		return false;
	}

	public void clearData() {
		data.delete(0, data.length());
	}

	public String getData() {
		return data.toString();
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getXsiType(Attributes atts) {
		return atts.getValue(XSI_NS, XSI_TYPE);
	}

	public String expandPrefix(String qName) {
		String result = qName;
		if (this.optTrackPrefixMapping && null != qName) {
			int idx = qName.indexOf(":");

			if (idx > 0) {
				try {
					String uri = this.prefixes.get(qName.substring(0, idx)).peek();
					result = uri + ((uri.endsWith(DataUtil.PATH_SEPARATOR)) ? "" : DataUtil.PATH_SEPARATOR)
							+ qName.substring(idx + 1);
				} catch (Exception e) {
					expandPrefix(qName);
					LogUtil.getInstance().log(getClass().getName(), Level.WARNING, "Unable to expandPrefix", e);
				}
			} else if (null != targetNamespace) {
				result = targetNamespace
						+ ((targetNamespace.endsWith(DataUtil.PATH_SEPARATOR)) ? "" : DataUtil.PATH_SEPARATOR) + qName;
			} else {
				try {
					String uri = this.prefixes.get("targetNamespace").peek();
					result = uri + ((uri.endsWith(DataUtil.PATH_SEPARATOR)) ? "" : DataUtil.PATH_SEPARATOR) + qName;
				} catch (Exception e) {
					LogUtil.getInstance().log(getClass().getName(), Level.WARNING, "Unable to expandPrefix", e);
				}
			}
		}
		return result;
	}

	public void setXMLReader(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	public <T> T unmarshal(Class<?> cls) {
		T result = XMLUtil.getInstance().unmarshal(cls, xmlReader, inputSource);

		return result;
	}

	public void setInputSource(InputSource is) {
		this.inputSource = is;

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		++depth;
		currentPath = null;
		if (optTrackPath) {
			if (null == path)
				path = new Stack<>();
			path.push(localName);
		}
		if (null == targetNamespace)
			targetNamespace = atts.getValue("targetNamespace");
		if (optClearDataOnElementStart && null != data && data.length() > 0)
			clearData();

	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (optTrackPrefixMapping && null != path){
			path.pop();
			currentPath = null;
		}
		--depth;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {

	}

	@Override
	public void endCDATA() throws SAXException {
	}

	@Override
	public void endDTD() throws SAXException {

	}

	@Override
	public void endEntity(String entity) throws SAXException {

	}

	@Override
	public void startCDATA() throws SAXException {

	}

	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {

	}

	@Override
	public void startEntity(String entity) throws SAXException {

	}
}