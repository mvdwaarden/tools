package xml;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import data.DataUtil;
import data.LogUtil;
import data.Targetdir;

public class XMLWriter implements Targetdir {
	protected StringBuilder out = new StringBuilder();
	protected DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	protected DocumentBuilder builder;
	protected Document document;
	protected Element root;
	protected Element huidige;
	protected String targetdir;

	public void init(String roottag) {
		init();
		root = document.createElement(roottag);
		root.setAttribute("created", new Date().toString());
		document.appendChild(root);
	}

	public void init(String namespace, String roottag, String[][] attrs) {
		init();
		root = document.createElementNS(namespace, roottag);

		for (String[] attr : attrs)
			root.setAttribute(attr[0], attr[1]);
		document.appendChild(root);

	}

	public Element getHuidige() {
		return huidige;
	}

	public void setHuidige(Element huidige) {
		this.huidige = huidige;
	}

	public void init() {
		if (null == builder)
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				LogUtil.getInstance().log(getClass().getName(), Level.WARNING, "can not create document builder", e);
			}
		if (null == document) {
			document = builder.newDocument();
		}
	}

	public void writeToFile() {
		writeToFile(null);
	}

	public void writeToFile(String filename) {
		if (null == filename)
			filename = root.getTagName() + ".xml";

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "string");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(bos);
			transformer.transform(source, result);
			if (null != targetdir)
				DataUtil.getInstance().writeToFile(targetdir + DataUtil.PATH_SEPARATOR + filename, bos.toString());
			else
				DataUtil.getInstance().writeToFile(filename, bos.toString());
		} catch (TransformerConfigurationException e) {
			LogUtil.getInstance().error("problem creating transformer", e);
		} catch (TransformerException e) {
			LogUtil.getInstance().error("problem creating transformer", e);
		}
	}

	public Element createElement(String tagName) {
		return document.createElement(tagName);
	}

	public Element createElement(String namespace, String tagName) {
		return document.createElementNS(namespace, tagName);
	}

	public Element getRoot() {
		return root;
	}

	public Document getDocument() {
		return document;
	}

	public void setRoot(Element root) {
		this.root = root;
		document.appendChild(this.root);
	}

	@Override
	public String getTargetdir() {
		return targetdir;
	}

	@Override
	public void setTargetdir(String targetdir) {
		this.targetdir = targetdir;
	}

	public Text createText(String value) {
		return document.createTextNode(value);
	}

}
