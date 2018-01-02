package xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import data.LogUtil;

/**
 * <pre>
 * This handler copies the XML file and maintains its semantics.
 * 
 * The syntaxis will differ on the following points:
 * - Explicit closing tags. 
 *   F.e. <a/> => <a></a>
 * - The <?xml version="1.0"?> 'header' is always inserted
 * - XML specific characters for content are ALL converted into non XML characters.
 *   F.e. &lt;xml-fragment/> => &lt;xml-fragment/&gt;
 * </pre>
 * 
 * @author mwa17610
 * 
 */
public class XMLSAXEchoHandler extends XMLSAXHandler {
	private OutputStream os;
	private Stack<List<String[]>> prefixes = new Stack<>();

	public XMLSAXEchoHandler(OutputStream os) {
		this.os = os;
	}

	protected void write(char[] ch, int start, int length) {

		write(new String(ch, start, length));
	}

	protected void writeEscaped(char[] ch, int start, int length) {
		write(XMLUtil.getInstance().escapeXML(new String(ch, start, length)));
	}

	protected void writeEscaped(String data) {
		write(XMLUtil.getInstance().escapeXML(data));
	}

	protected void write(String str) {
		try {
			byte[] bytes = str.getBytes();
			os.write(bytes, 0, bytes.length);
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to write data", e);
		}
	}

	public void superCharacters(char[] ch, int start, int length) {
		super.characters(ch, start, length);
	}

	public void writeCharacters(String data) {
		writeEscaped(data);
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		superCharacters(ch, start, length);
		writeCharacters(new String(ch,start,length));
	}

	@Override
	public void endDocument() {
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		superEndElement(uri, localName, qName);
		writeEndElement(uri, localName, qName);
	}

	@Override
	public void endPrefixMapping(String prefix) {
		// skip super end prefix mapping !super.endPrefixMapping(prefix);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) {
		superIgnorableWhitespace(ch, start, length);
		writeIgnorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data) {
		superProcessingInstruction(target, data);
		writeProcessingInstruction(target, data);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		super.setDocumentLocator(locator);

	}

	@Override
	public void skippedEntity(String name) {
		super.skippedEntity(name);

	}

	public void superStartDocument() {
		super.startDocument();
	}

	public void writeStartDocument() {
		write("<?xml version=\"1.0\"?>");
	}

	@Override
	public void startDocument() {
		superStartDocument();
		writeStartDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		superStartElement(uri, localName, qName, atts);
		writeStartElement(uri, localName, qName, atts);
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) {
		// skip super prefix mapping! super.startPrefixMapping(prefix, uri);
		if (depth <= prefixes.size())
			prefixes.push(new ArrayList<>());
		prefixes.peek().add(new String[] { prefix, uri });
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		write("<!--");
		write(ch, start, length);
		write("-->");
	}

	@Override
	public void endCDATA() throws SAXException {
		write("]]>");
	}

	@Override
	public void endDTD() throws SAXException {

	}

	@Override
	public void endEntity(String entity) throws SAXException {

	}

	@Override
	public void startCDATA() throws SAXException {
		write("<![CDATA[");
	}

	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {

	}

	@Override
	public void startEntity(String entity) throws SAXException {

	}

	public OutputStream getOutputStream() {
		return os;
	}

	public void superStartElement(String uri, String localName, String qName, Attributes atts) {
		super.startElement(uri, localName, qName, atts);
		if (prefixes.size() < depth)
			prefixes.push(new ArrayList<>());
	}

	public void writeStartElement(String uri, String localName, String qName, Attributes atts) {

		write("<" + qName);
		// skip additional namespace prefixes
		// for (String[] mapping : prefixes.peek())
		// write(" xmlns:" + mapping[0] + "=\"" + mapping[1] + "\"");

		for (int i = 0; i < atts.getLength(); ++i)
			write(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");

		write(">");
	}

	public void superEndElement(String uri, String localName, String qName) {
		super.endElement(uri, localName, qName);
		if (depth < prefixes.size())
			prefixes.pop();
	}

	public void writeEndElement(String uri, String localName, String qName) {
		write("</" + qName + ">");
	}

	public void superProcessingInstruction(String target, String data) {
		super.processingInstruction(target, data);
	}

	public void writeProcessingInstruction(String target, String data) {
		write("<?" + target + " " + data + "?>");
	}

	public void superIgnorableWhitespace(char[] ch, int start, int length) {
		super.ignorableWhitespace(ch, start, length);
	}

	public void writeIgnorableWhitespace(char[] ch, int start, int length) {
		write(ch, start, length);
	}
}
