package xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Maakt text regels van een XML. Daarna worden de XML regels oplopend
 * gesorteerd.
 * 
 * <pre>
 * Deze content handler kan worden toegepast om XML documenten met elkaar te vergelijken.
 * 
 * Twee getextualizde documenten (waarop deze content handler is toegepast) kunnen met een
 * eenvoudige compair worden vegeleken.
 * 
 * B.v. <a><b><c>c waarde</c></b></a> wordt (in het voorbeeld is '[' 'kleiner' dan '.'  :
 * a[]
 * a.b []
 * a.b.c [c waarde]
 * 
 * Opmerking: 25/07/2014
 * Op het moment wordt de data in de hierarchie niet meegenomen dit zou kunnen betekenen
 * dat indien twee waarden in een hierarchie verwisseld worden wel gelijk aan elkaar gesteld worden.
 * 
 * B.v. <a><b><c>c1 waarde</c><c>c2 waarde</c></b><b><c>c3 waarde</c></b></a> is dan identiek aan
 * <a><b><c>c1 waarde</c><c>c3 waarde</c></b><b><c>c2 waarde</c></b></a>
 * 
 * In beide gevallen is de textualize:
 * a[]
 * a.b []
 * a.b []
 * a.b.c [c1 waarde]
 * a.b.c [c2 waarde]
 * a.b.c [c3 waarde]
 * 
 * In principe gaat de plek in de hierarchie verloren.
 * 
 * <pre>
 */
public class TextualizeHandler implements ContentHandler {
	private Map<String, String> nsMap;
	private Line line;
	private LineStack stack;
	private List<String> lines = new ArrayList<>();

	public TextualizeHandler(Map<String, String> nsMap) {
		this.nsMap = new HashMap<>();
		stack = new LineStack(nsMap);

		if (null != nsMap) {
			for (Entry<String, String> e : nsMap.entrySet()) {
				this.nsMap.put(e.getKey(), e.getValue());
			}
		}
	}

	public List<String> getLines() {
		return lines;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		String tmp = nsMap.get(uri);

		if (null == tmp)
			nsMap.put(prefix, uri);

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// who cares in our case (only ONE prefix allowed in whole stream)
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		line = new Line();
		stack.push(atts, uri, localName);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		StringBuilder tmp = new StringBuilder();
		tmp.append(stack.getPath());
		tmp.append(stack.getAttributes());
		tmp.append("[");
		tmp.append(line.getText());
		tmp.append("]");
		lines.add(tmp.toString());
		stack.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String tmp = new String(ch, start, length);
		// ignore whitespace
		line.textAppend(tmp);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {

	}

	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	private static class Line {
		private StringBuilder text = new StringBuilder();

		public void textAppend(String str) {
			text.append(str.replace("\n", "").replace("\t", "").replace("\r", ""));
		}

		public String getText() {
			return text.toString();
		}
	}

	private static class LineStack {
		private Map<String, String> nsMap;
		private Stack<Map<String, String>> attributes = new Stack<>();
		private Stack<String> elements = new Stack<>();
		private Stack<String> uris = new Stack<>();

		public LineStack(Map<String, String> nsMap) {
			this.nsMap = nsMap;
		}

		public void push(Attributes attributes, String uri, String element) {
			int end = attributes.getLength();
			if (end > 0) {
				Map<String, String> tmp = new HashMap<>();
				for (int i = 0; i < end; ++i) {
					tmp.put(attributes.getLocalName(i), attributes.getValue(i));
				}
				this.attributes.push(tmp);
			} else {
				this.attributes.push(null);
			}
			this.uris.push(uri);
			this.elements.push(element);
		}

		public void pop() {
			this.attributes.pop();
			this.uris.pop();
			this.elements.pop();

		}

		@SuppressWarnings("unused")
		public int getSize() {
			return attributes.size();
		}

		@SuppressWarnings("unused")
		public Map<String, String> peekAttributes() {
			return attributes.peek();
		}

		@SuppressWarnings("unused")
		public String peekElement() {
			return elements.peek();
		}

		@SuppressWarnings("unused")
		public String peekUri() {
			return uris.peek();
		}

		public String getPath() {
			StringBuilder result = new StringBuilder();

			// make the path (concat uri-prefix with element name)
			for (int i = 0; i < elements.size(); ++i) {
				String uri = uris.get(i);
				String element = elements.get(i);
				// get
				String prefix = nsMap.get(uri);
				if (null != prefix)
					result.append(prefix + ":" + element);
				else
					result.append(element);

			}

			return result.toString();

		}

		public String getAttributes() {
			// space separated name value pairs.
			StringBuilder result = new StringBuilder();
			Map<String, String> atts = attributes.peek();
			if (null != atts) {

				String[] names = new String[atts.size()];

				int i = 0;
				for (String str : atts.keySet())
					names[i++] = str;
				Arrays.sort(names);

				for (int n = 0; n < atts.size(); ++n) {
					result.append(" ");
					result.append(names[n]);
					result.append("=\'");
					result.append(XMLUtil.getInstance().cvtString2Xml(atts.get(names[n])));
					result.append("'");
				}
			}
			return result.toString();
		}
	}
}
