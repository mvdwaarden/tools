package nl.ordina.tools.gen;

import java.util.List;

import org.w3c.dom.Element;

import data.DataUtil;
import data.Targetdir;
import metadata.MetaAtom;
import metadata.MetaAtom.BaseType;
import metadata.MetaComposite;
import metadata.MetaData;
import metadata.MetaElement;
import metadata.MetaEnumeration;
import metadata.MetaType;
import xml.XMLWriter;

/**
 * XSD Generator
 * 
 * @author mwa17610
 * 
 */
public class XSDGenerator implements Targetdir {
	private XMLWriter writer;
	private String targetNamespace = "http://tempuri.org";
	private String rootTag = "schema";
	private String targetdir;

	@Override
	public String getTargetdir() {
		return targetdir;
	}

	@Override
	public void setTargetdir(String targetdir) {
		this.targetdir = targetdir;
	}

	public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public XSDGenerator() {
	}

	public void generate(MetaData metadata) {
		writer = new XMLWriter();
		this.rootTag = metadata.getRootTag();
		beginDocument();
		// Schrijf elementen weg
		for (MetaElement element : metadata.getRoots()) {
			writeElement(metadata, writer.getRoot(), element);
		}
		// Schrijf complexe types weg
		for (MetaType type : metadata.getTypes()) {
			if (type instanceof MetaComposite)
				writeComplexType(metadata, writer.getRoot(), (MetaComposite) type);
		}
		// Schrijf simple types weg
		for (MetaType type : metadata.getTypes()) {
			if (type instanceof MetaAtom)
				writeSimpleType(metadata, writer.getRoot(), (MetaAtom) type);
		}
		endDocument();
	}

	private void writeElement(MetaData metadata, Element parent, MetaElement el) {
		Element element = writer.createElement(NS_XSD, "element");
		parent.appendChild(element);
		element.setAttribute("name", el.getName());
		element.setAttribute("type", "tns:" + el.getType().getName());
	}

	private void writeSimpleType(MetaData metadata, Element parent, MetaAtom atom) {
		Element simpleType = writer.createElement(NS_XSD, "simpleType");
		parent.appendChild(simpleType);

		simpleType.setAttribute("name", atom.getName());
		Element restriction = writer.createElement(NS_XSD, "restriction");
		simpleType.appendChild(restriction);
		restriction.setAttribute("base", toXsdType(atom.getBaseType()));
		for (MetaEnumeration enumeration : atom.getEnumerations()) {
			Element elEnum = writer.createElement(NS_XSD, "enumeration");
			elEnum.setAttribute("value", enumeration.getCode());
			restriction.appendChild(elEnum);
		}
	}

	private String toXsdType(BaseType baseType) {
		String[][] map = new String[][] { { BaseType.BOOLEAN.name(), "boolean" }, { BaseType.STRING.name(), "string" },
				{ BaseType.FLOAT.name(), "float" }, { BaseType.INT.name(), "integer" },
				{ BaseType.DATE.name(), "string" }, { BaseType.DATETIME.name(), "string" } };
		String result = null;
		for (String[] e : map) {
			if (e[0].equals(baseType.name())) {
				result = e[1];
				break;
			}
		}

		return result;
	}

	private void writeComplexType(MetaData metadata, Element parent, MetaComposite type) {
		Element complexType = writer.createElement(NS_XSD, "complexType");
		parent.appendChild(complexType);
		complexType.setAttribute("name", type.getName());
		Element sequence = writer.createElement(NS_XSD, "sequence");
		complexType.appendChild(sequence);
		for (MetaElement child : type.getElements()) {
			Element el = writer.createElement(NS_XSD, "element");
			el.setAttribute("type", ((child.getType() instanceof BaseType) ? "" : "tns:")
					+ GenerationCustomizationUtil.getInstance().getXsdAttributeType(child));
			List<String> documentation = GenerationCustomizationUtil.getInstance().getXsdDocumentation(child);
			if (!documentation.isEmpty()) {
				for (String docu : documentation)
					addDocumentation(el, docu);
				el.setAttribute("name", GenerationCustomizationUtil.getInstance().makePublicName(child) + "_BMG");
			} else
				el.setAttribute("name", GenerationCustomizationUtil.getInstance().makePublicName(child));

			if (child.getMaxAantal() == -1)
				el.setAttribute("maxOccurs", "unbounded");
			else if (child.getMaxAantal() > 0)
				el.setAttribute("maxOccurs", "" + child.getMaxAantal());
			sequence.appendChild(el);
		}
	}

	public void beginDocument() {
		writer.init();
		writer.setRoot(writer.createElement(NS_XSD, "schema"));
		writer.getRoot().setAttribute("targetNamespace", getTargetNamespace());
		writer.getRoot().setAttribute("xmlns:tns", getTargetNamespace());
	}

	public void endDocument() {
		writer.writeToFile(getTargetdir() + DataUtil.PATH_SEPARATOR + rootTag + ".xsd");
	}

	public void addDocumentation(Element el, String documentation) {
		Element annot = writer.createElement(NS_XSD, "annotation");
		el.appendChild(annot);
		Element doc = writer.createElement(NS_XSD, "documentation");
		annot.appendChild(doc);
		doc.setNodeValue(documentation);
	}
}
