package metadata;

import java.util.Stack;

import org.xml.sax.Attributes;

import data.LogUtil;
import metadata.MetaAtom.BaseType;
import xml.XMLSAXHandler;
import xml.XMLUtil;

/**
 * Initialize MetaData information from a XSD
 * 
 * @author mwa17610
 * 
 */
public class SimpleXSDReader {
	public MetaData read(String file) {
		MetaData result = new MetaData();

		try {
			XSDHandler handler = new XSDHandler();
			XMLUtil.getInstance().parse(file, handler);
			result = handler.getMetaData();
			result.resolveTypes();
		} catch (Exception e) {
			LogUtil.getInstance().error("problem parsing [" + file + "]", e);
		}

		return result;
	}

	public static class XSDHandler extends XMLSAXHandler {
		private static final String EL_ELEMENT = "element";
		private static final String EL_ENUMERATION = "enumeration";
		private static final String EL_LENGTH = "length";
		private static final String EL_PATTERN = "pattern";
		private static final String EL_TOTAL_DIGITS = "totalDigits";
		private static final String EL_FRACTION_DIGITS = "fractionDigits";
		private static final String EL_SIMPLE_TYPE = "simpleType";
		private static final String EL_COMPLEX_TYPE = "complexType";
		private static final String EL_GROUP = "group";
		private static final String EL_DOCUMENTATION = "documentation";
		private static final String EL_EXTENSION = "extension";
		private static final String EL_RESTRICTION = "restriction";		
		private static final String ATTRIBUTE_NAME = "name";
		private static final String ATTRIBUTE_ID = "id";
		private static final String ATTRIBUTE_VALUE = "value";
		private static final String ATTRIBUTE_REF = "ref";
		private static final String ATTRIBUTE_MAX_AANTAL = "maxOccurs";
		private static final String ATTRIBUTE_MIN_AANTAL = "minOccurs";
		private static final String ATTRIBUTE_TYPE = "type";
		private static final String ATTRIBUTE_VALUE_UNBOUNDED = "unbounded";
		private static final String ATTRIBUTE_BASE_TYPE = "base";
		private static final String ATTRIBUTE_TARGET_NAMESPACE = "targetNamespace";
		private MetaData metadata = new MetaData();
		private Stack<MetaElement> stack = new Stack<>();
		private MetaComposite currentType;
		private MetaAtom atom;
		private int groupRef;

		public MetaData getMetaData() {
			return metadata;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			super.startElement(uri, localName, qName, atts);
			String targetNamespace = atts.getValue(ATTRIBUTE_TARGET_NAMESPACE);
			if (null != targetNamespace && !targetNamespace.isEmpty())
				metadata.setTargetNamespace(targetNamespace);

			if (localName.equals(EL_ELEMENT)) {
				// assume ALL types are local
				String type = getTypeName(atts.getValue(ATTRIBUTE_TYPE));
				BaseType bt = getBaseTypeFromXsdType(type);
				String id = atts.getValue(ATTRIBUTE_ID);
				String name = atts.getValue(ATTRIBUTE_NAME);
				String maxAantal = atts.getValue(ATTRIBUTE_MAX_AANTAL);
				String minAantal = atts.getValue(ATTRIBUTE_MIN_AANTAL);
				MetaElement el = new MetaElement();
				el.setTag(name);
				el.setName(name);
				if (null == bt)
					el.setType((null == type || type.length() == 0) ? MetaData.COMPLEX_TYPE_NAME : type);
				else
					el.setType(bt);
				if (null != id && !id.isEmpty())
					el.setId(Integer.parseInt(id));				
				if (null != maxAantal)
					el.setMaxAantal((maxAantal.equals(ATTRIBUTE_VALUE_UNBOUNDED)) ? -1 : Integer.parseInt(maxAantal));
				if (null != minAantal && Integer.parseInt(minAantal) == 0)
					el.setMandatory(false);
				else
					el.setMandatory(true);
				addElement(el);
				stack.push(el);
			} else if (localName.equals(EL_SIMPLE_TYPE)) {
				String name = getTypeName(atts.getValue(ATTRIBUTE_NAME));
				if (null != name && !name.isEmpty()) {
					atom = new MetaAtom();
					atom.setName(name);
				}
			} else if (localName.equals(EL_GROUP)) {
				String name = getTypeName(atts.getValue(ATTRIBUTE_NAME));
				String ref = getTypeName(atts.getValue(ATTRIBUTE_REF));

				if (name != null && !name.isEmpty()) {
					MetaGroup group = new MetaGroup();
					currentType = group;
					group.setName(getTypeName(atts.getValue(ATTRIBUTE_NAME)));
					if (group.getName() == null || group.getName().length() == 0)
						group.setName(stack.peek().getName());
					if (!stack.isEmpty())
						stack.peek().setType(group);
					metadata.addType(group);
				} else if (ref != null && !ref.isEmpty()) {
					MetaElement el = new MetaElement();
					String id = atts.getValue(ATTRIBUTE_ID);
					if (null != id && !id.isEmpty())
						el.setId(Integer.parseInt(id));
					el.setType(ref);
					el.setName(ref + "_" + ++groupRef);
					addElement(el);
				}
			} else if (localName.equals(EL_COMPLEX_TYPE)) {
				MetaComposite composite = new MetaComposite();
				currentType = composite;
				composite.setName(getTypeName(atts.getValue(ATTRIBUTE_NAME)));
				if (composite.getName() == null || composite.getName().length() == 0)
					composite.setName(stack.peek().getName());
				if (!stack.isEmpty())
					stack.peek().setType(composite);
				metadata.addType(composite);
			} else if (null != atom) {
				if (localName.equals(EL_EXTENSION) || localName.equals(EL_RESTRICTION)) {
					BaseType bt = getBaseTypeFromXsdType(atts.getValue(ATTRIBUTE_BASE_TYPE));
					if (null == bt)
						bt = BaseType.STRING;
					atom.setBaseType(bt);
				} else if (localName.equals(EL_PATTERN)) {
					atom.setPattern(atts.getValue(ATTRIBUTE_VALUE));
				} else if (localName.equals(EL_LENGTH)) {
					atom.setLength(Integer.parseInt(atts.getValue(ATTRIBUTE_VALUE)));
				} else if (localName.equals(EL_TOTAL_DIGITS)) {
					atom.setLength(Integer.parseInt(atts.getValue(ATTRIBUTE_VALUE)));
				} else if (localName.equals(EL_FRACTION_DIGITS)) {
					atom.setFractionLength(Integer.parseInt(atts.getValue(ATTRIBUTE_VALUE)));
				} else if (localName.equals(EL_ENUMERATION)) {
					atom.getEnumerations()
							.add(new MetaEnumeration(atts.getValue(ATTRIBUTE_VALUE), atts.getValue(ATTRIBUTE_VALUE)));
				}
			}
		}

		private String getTypeName(String fulltype) {
			String[] fulltypes = (null != fulltype && !fulltype.isEmpty()) ? fulltype.split(":") : new String[] {};
			String result = "";

			if (fulltypes.length > 1)
				result = fulltypes[1];
			else
				result = fulltype;

			return result;
		}

		private void addElement(MetaElement el) {
			if (null != el.getName() && !el.getName().isEmpty()) {
				if (!stack.isEmpty()) {
					el.setParent(stack.peek());
					if (stack.peek().getType() instanceof MetaComposite)
						stack.peek().<MetaComposite> getType().addElement(el);
				} else if (pathTest("schema/element")) {
					metadata.addRoot(el);
				} else if (null != currentType)
					currentType.addElement(el);
			}
		}

		private BaseType getBaseTypeFromXsdType(String xsdType) {
			BaseType result = null;
			if (null != xsdType) {
				String[] fqTypeName = xsdType.split(":");
				String type = "";

				/* <String,BaseType> */
				Object[][] xsdMap = new Object[][] { { "string", BaseType.STRING }, { "int", BaseType.INT },
						{ "integer", BaseType.INT }, { "long", BaseType.INT }, { "nonNegativeInteger", BaseType.INT },
						{ "date", BaseType.DATE }, { "boolean", BaseType.BOOLEAN }, { "gYear", BaseType.INT } };
				if (fqTypeName.length == 1) {
					type = fqTypeName[0];
				} else if (fqTypeName.length == 2) {
					type = fqTypeName[1];
				}
				for (Object[] row : xsdMap) {
					if (row[0].equals(type)) {
						result = (BaseType) row[1];
						break;
					}
				}
			}

			return result;
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			if (localName.equals(EL_ELEMENT)) {
				stack.pop();
			} else if (localName.equals(EL_COMPLEX_TYPE)) {
			} else if (localName.equals(EL_SIMPLE_TYPE) && null != atom) {
				metadata.addType(atom);
				atom = null;
			} else if (localName.equals(EL_DOCUMENTATION)) {
				if (!stack.isEmpty())
					stack.peek().addDocumentation(getData().toString());

			}
			super.endElement(uri, localName, qName);
		}
	}
}
