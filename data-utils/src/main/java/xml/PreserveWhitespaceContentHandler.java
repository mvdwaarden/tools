package xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class PreserveWhitespaceContentHandler implements ContentHandler {
	private ContentHandler delegate;

	public PreserveWhitespaceContentHandler(ContentHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		if (null != delegate)
			delegate.setDocumentLocator(locator);

	}

	@Override
	public void startDocument() throws SAXException {
		if (null != delegate)
			delegate.startDocument();

	}

	@Override
	public void endDocument() throws SAXException {
		if (null != delegate)
			delegate.endDocument();

	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (null != delegate)
			delegate.startPrefixMapping(prefix, uri);

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		if (null != delegate)
			delegate.endPrefixMapping(prefix);

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		delegate.startElement(uri, localName, qName, atts);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (null != delegate)
			delegate.endElement(uri, localName, qName);

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (null != delegate)
			delegate.characters(ch, start, length);

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (null != delegate)
			delegate.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		if (null != delegate)
			delegate.processingInstruction(target, data);

	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		if (null != delegate)
			delegate.skippedEntity(name);
	}
}
