package xml;

import org.xml.sax.Attributes;

public interface XMLSimpleChecker {
	enum CheckEvent {
		START_ELEMENT, END_ELEMENT;
	}

	boolean check(CheckEvent event, String path, String uri, String localName, String qName, Attributes atts,
			StringBuilder data);
}
