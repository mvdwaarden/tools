package xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ParseConversionEvent;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import data.ConfigurationUtil;
import data.DataUtil;
import data.EnumUtil;
import data.LogUtil;
import data.StringUtil;
import data.Util;
import object.ObjectUtil;
import xml.XMLSimpleChecker.CheckEvent;

/**
 * Purpose: Utility class for XML related functions
 */
public class XMLUtil implements Util {
	public static final String EXCLUDED_HOSTNAMES = "xml.excluded.hostnames";
	public static final String CDATA_START = "<![CDATA[";
	public static final String CDATA_END = "]]>";
	public static final String TRANSFORMER_FACTORY_CLASS = "xml.transformer.factory";
	public static final String PARSER_FACTORY_CLASS = "xml.parser.factory";
	public static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";
	private static final ThreadLocal<XMLUtil> instance = new ThreadLocal<XMLUtil>();
	/**
	 * XML escaping methods.
	 */
	private static final String[][] XML_ESCAPE_STR_TO_XML = new String[][] { { "&", "&amp;" }, { "'", "&apos;" },
			{ "\"", "&quot;" }, { ">", "&gt;" }, { "<", "&lt;" } };

	public enum Option {
		NOP, XML_UNMARSHAL_PRESERVE_LINEFEED
	}

	/**
	 * 
	 * @return
	 */
	public SAXParser getSaxParser() {
		SAXParserFactory saxfactory = null;
		SAXParser parser = null;

		try {
			String parserFactoryName = ConfigurationUtil.getInstance().getSetting(PARSER_FACTORY_CLASS, null);

			if (null == parserFactoryName || parserFactoryName.isEmpty()) {
				saxfactory = SAXParserFactory.newInstance();
				parser = saxfactory.newSAXParser();
			} else {
				parser = ObjectUtil.getInstance().invoke(Class.forName(parserFactoryName), "newInstance",
						new Class[] { Map.class }, new Object[] { null });
			}

			// make sure the namespaces information is passed to the SAX handler
			parser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", true);
			parser.getXMLReader().setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		} catch (ParserConfigurationException | SAXException | ClassNotFoundException e) {
			throw new RuntimeException("check SAX configuration", e);
		}

		return parser;
	}

	public boolean isXML(String data) {
		boolean result = false;
		
		if (null != data && data.contains("<")) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			parse(new ByteArrayInputStream(data.getBytes()), new XMLSAXEchoHandler(bos));
			result = !bos.toString().isEmpty();
		}
		
		return result;
	}

	/**
	 * Get a thread specific instance of the XML utility class.
	 * 
	 * @return
	 */
	public static XMLUtil getInstance() {
		XMLUtil result = instance.get();

		if (null == result) {
			result = new XMLUtil();
			instance.set(result);
		}

		return result;
	}

	public String cvtString2Xml(String str) {
		String result = str;

		for (String[] escape : XML_ESCAPE_STR_TO_XML)
			result = result.replace(escape[0], escape[1]);

		return result;
	}

	public String cvtXml2String(String str) {
		String result = str;
		int end = XML_ESCAPE_STR_TO_XML.length;

		for (int i = end - 1; i >= 0; --i)
			result = result.replace(XML_ESCAPE_STR_TO_XML[i][1], XML_ESCAPE_STR_TO_XML[i][0]);

		return result;
	}

	/**
	 * Base 64 encoding methods
	 * 
	 * @param str
	 * @return
	 */
	public byte[] cvtBase642Bytes(String str) {
		return DatatypeConverter.parseBase64Binary(str);
	}

	public String cvtBytes2Base64(byte[] data) {
		return DatatypeConverter.printBase64Binary(data);
	}

	/**
	 * Hash method
	 * 
	 * @param text
	 * @param algoritm
	 * @return
	 */
	public byte[] getHash(String text, String algoritm) {
		byte[] result = new byte[] {};

		try {
			MessageDigest digest = MessageDigest.getInstance(algoritm);
			result = digest.digest(text.getBytes());
		} catch (NoSuchAlgorithmException e) {
			LogUtil.getInstance().warning("no digest algoritm with [" + algoritm + "]", e);
		}

		return result;
	}

	/**
	 * Purpose: Getextualized XML documents can be easily compared using simple
	 * String compare methods
	 * 
	 * @param is
	 * @param os
	 * @param nsMap
	 */
	public void textualize(InputStream is, OutputStream os, Map<String, String> nsMap) {
		SAXParser parser = XMLUtil.getInstance().getSaxParser();
		TextualizeHandler handler = new TextualizeHandler(nsMap);
		List<String> lines;
		String[] buf;

		try {
			parser.getXMLReader().setContentHandler(handler);
			parser.getXMLReader().parse(new InputSource(is));
			lines = handler.getLines();
			buf = new String[lines.size()];
			Arrays.sort(lines.toArray(buf));
			for (int i = 0; i < buf.length; ++i) {
				if (null != buf[i]) {
					os.write(buf[i].getBytes());
					os.write("\n".getBytes());
				}
			}
		} catch (IOException | SAXException e) {
			LogUtil.getInstance().warning("problem normalizing ", e);
		}
	}

	/**
	 * Unescape Xml
	 */
	public String unescapeXML(String data) {
		data = data.replace("&apos;", "'");
		data = data.replace("&gt;", ">");
		data = data.replace("&lt;", "<");
		data = data.replace("&amp;", "&");

		return data;
	}

	/**
	 * Escape Xml
	 * 
	 * @param data
	 * @return
	 */
	public String escapeXML(String data) {
		data = data.replace("&", "&amp;");
		data = data.replace("<", "&lt;");
		data = data.replace(">", "&gt;");
		data = data.replace("'", "&apos;");
		return data;
	}

	public static void main(String[] args) throws FileNotFoundException {
		XMLUtil util = new XMLUtil();

		ByteArrayOutputStream os1 = new ByteArrayOutputStream();
		ByteArrayOutputStream os2 = new ByteArrayOutputStream();
		Map<String, String> nsMap = new HashMap<>();

		util.textualize(new FileInputStream("d:/tmp/compare/file1.xml"), os1, nsMap);
		util.textualize(new FileInputStream("d:/tmp/compare/file2.xml"), os2, nsMap);

	}

	public <T> T readFromFile(String filename, Class<?> cls) {
		return readFromFile(filename, cls);
	}

	public <T> T readFromFile(String filename, Class<?> cls, Option... options) {
		T result = null;
		try {
			SAXParser saxParser = getSaxParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			if (EnumUtil.getInstance().contains(options, Option.XML_UNMARSHAL_PRESERVE_LINEFEED))
				LogUtil.getInstance().warning("preserve line feed option detected, which has currently no effect");
			result = unmarshal(cls, xmlReader, new InputSource(filename));
		} catch (SAXException e) {
			LogUtil.getInstance().info("error reading [" + filename + "]", e);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T unmarshal(Class<?> cls, XMLReader xmlReader, InputSource inputSource) {
		T result = null;
		SAXSource source = new SAXSource(xmlReader, inputSource);
		try {
			JAXBContext ctx = JAXBContext.newInstance(cls);
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			unmarshaller.setEventHandler(new ValidationEventHandler() {

				@Override
				public boolean handleEvent(ValidationEvent event) {
					if (event instanceof ParseConversionEvent) {
						ParseConversionEvent parseEvent = (ParseConversionEvent) event;

						parseEvent.getMessage();
					}
					return true;
				}

			});
			result = (T) unmarshaller.unmarshal(source);
		} catch (JAXBException e) {
			LogUtil.getInstance().warning("error reading from [" + inputSource.getSystemId() + "]", e);
		}

		return result;
	}

	public <T> String marshall(T obj, Class<?> cls, boolean useWrapper) {
		String result = null;
		JAXBContext ctx;

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ctx = JAXBContext.newInstance(cls);
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			if (useWrapper) {
				@SuppressWarnings("unchecked")
				JAXBElement<T> el = new JAXBElement<T>(new QName(cls.getSimpleName()), (Class<T>) obj.getClass(), obj);
				marshaller.marshal(el, bos);
			} else {
				marshaller.marshal(obj, bos);
			}
			result = bos.toString();
		} catch (JAXBException e) {
			LogUtil.getInstance().info("error marshalling [" + cls.getName() + "]", e);
		}

		return result;
	}

	public <T> void writeToFile(String filename, T obj, Class<?> cls, boolean useWrapper) {
		DataUtil.getInstance().writeToFile(filename, marshall(obj, cls, useWrapper));
	}

	public <T> void writeToFile(String filename, T obj, Class<?> cls) {
		writeToFile(filename, obj, cls, false);
	}

	/**
	 * Performs a check on XML file
	 * 
	 * @param filename
	 * @param checker
	 * @param options
	 * @return
	 */
	public boolean check(String filename, XMLSimpleChecker checker, Option... options) {
		class Locals {
			boolean result = false;
		}
		;
		final Locals _locals = new Locals();

		parse(filename, new XMLSAXHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes atts) {
				super.startElement(uri, localName, qName, atts);
				if (!_locals.result)
					_locals.result = checker.check(CheckEvent.START_ELEMENT, getPath(), uri, localName, qName, atts,
							null);
			}

			@Override
			public void endElement(String uri, String localName, String qName) {
				super.endElement(uri, localName, qName);
				if (!_locals.result)
					_locals.result = checker.check(CheckEvent.END_ELEMENT, getPath(), uri, localName, qName, null,
							data);
			}
		});
		if (_locals.result) {
			_locals.result = true;
		}
		return _locals.result;
	}

	/**
	 * Parse an XML using a content handler
	 * 
	 * @param filename
	 * @param handler
	 * @param options
	 */
	public boolean parse(InputStream stream, ContentHandler handler, Option... options) {
		return parse(new InputSource(stream), handler, "stream", options);
	}

	/**
	 * Parse an XML using a content handler
	 * 
	 * @param filename
	 * @param handler
	 * @param options
	 */
	public boolean parse(String filename, ContentHandler handler, Option... options) {
		return parse(new InputSource(DataUtil.getInstance().convertToFileURL(filename)), handler, filename, options);
	}

	/**
	 * Parse an XML using a content handler
	 * 
	 * @param is
	 * @param handler
	 * @param context
	 * @param options
	 */
	private boolean parse(InputSource is, ContentHandler handler, String context, Option... options) {
		boolean result = false;
		try {
			if (null == is.getSystemId() || !isExcluded(is.getSystemId())) {
				SAXParser saxParser = getSaxParser();
				if (EnumUtil.getInstance().contains(options, Option.XML_UNMARSHAL_PRESERVE_LINEFEED))
					LogUtil.getInstance().warning("preserve line feed option detected, which has currently no effect");
				saxParser.getXMLReader().setContentHandler(handler);
				if (handler instanceof XMLSAXHandler) {
					saxParser.setProperty(LEXICAL_HANDLER_PROPERTY, handler);
				}
				saxParser.getXMLReader().parse(is);
				result = true;
			} else {
				LogUtil.getInstance().warning("excluced [" + is.getSystemId() + "] from parsing");
				result = true;
			}
		} catch (Exception e) {
			LogUtil.getInstance().error("error parsing [" + context + "]", e);
		}
		return result;
	}

	/**
	 * Check if a systemId is excluded
	 * 
	 * @param systemId
	 * @return
	 */
	public boolean isExcluded(String systemId) {
		boolean result;

		String hostname = StringUtil.getInstance().replace(systemId, "http?:\\/\\/([^\\/:]*)(:|\\/).*,$1");
		String exclude = ConfigurationUtil.getInstance().getSetting(EXCLUDED_HOSTNAMES, "");
		result = exclude.contains(hostname);

		return result;
	}

	/**
	 * Validate an XML against an XSD and retrieve a full list of validation
	 * errors
	 * 
	 * @param xsdFilename
	 * @param xmlFilename
	 * @return
	 */
	public List<Exception> validate(String xsdFilename, String xmlFilename) {
		List<Exception> result = new ArrayList<>();

		try {
			XMLStreamReader reader = XMLInputFactory.newInstance()
					.createXMLStreamReader(new FileInputStream(xmlFilename));
			SchemaFactory validationFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = validationFactory.newSchema(new File(xsdFilename));
			Validator validator = schema.newValidator();
			// registration of inner class to detect multiple errors.
			validator.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					result.add(exception);
				}

				@Override
				public void error(SAXParseException exception) throws SAXException {
					result.add(exception);
				}

				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					result.add(exception);
				}
			});
			validator.validate(new StAXSource(reader));
		} catch (IOException | SAXException | XMLStreamException e) {
			LogUtil.getInstance().error("could not validate [" + xmlFilename + "] against [" + xsdFilename + "]", e);
		} catch (FactoryConfigurationError e) {
			LogUtil.getInstance().error("could not validate [" + xmlFilename + "] against [" + xsdFilename + "]",
					new Exception(e));
		}

		return result;
	}

	/**
	 * Evaluate XPath expression on an XML
	 * 
	 * @param is
	 * @param xpath
	 * @param namespaces
	 * @return
	 */
	public String evaluate(InputStream is, String xpath, final Map<String, String> namespaces) {
		String result = "";
		XPathFactory fac = XPathFactory.newInstance();

		XPath xp = fac.newXPath();
		NamespaceContext nsContext = new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				String result = namespaces.get(prefix);

				return result;
			}

			@Override
			public String getPrefix(String namespaceURI) {
				String result = "";

				Optional<String> oResult = namespaces.entrySet().stream().filter(e -> e.getValue().equals(namespaceURI))
						.findFirst().map(e -> e.getKey());
				if (oResult.isPresent())
					result = oResult.get();

				return result;
			}

			@Override
			public Iterator<String> getPrefixes(String namespaceURI) {
				List<String> prefixes = namespaces.entrySet().stream().filter(e -> e.getValue().equals(namespaceURI))
						.map(e -> e.getKey()).collect(Collectors.toList());

				return prefixes.iterator();
			}
		};
		xp.setNamespaceContext(nsContext);

		try {
			result = xp.evaluate(xpath, new InputSource(is));
		} catch (XPathExpressionException e) {
			LogUtil.getInstance().error("could not evaluate [" + xpath + "]", e);
		}

		return result;

	}

	public String cvtCDATA2String(String data) {
		String result = data;

		if (data.indexOf(CDATA_START) == 0 && data.lastIndexOf(CDATA_END) == data.length() - CDATA_END.length())
			result = data.substring(CDATA_START.length(), data.length() - CDATA_END.length());

		return result;
	}

	public String stripXMLHeader(String xml) {
		String result = xml;
		if (xml.startsWith("<?xml version=")) {
			int idx = xml.indexOf("?>");
			if (idx > 0)
				result = xml.substring(idx + "?>".length());

		}
		return result;
	}
}
