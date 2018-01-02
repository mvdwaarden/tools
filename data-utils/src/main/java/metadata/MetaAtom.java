package metadata;

import java.util.ArrayList;
import java.util.List;

public class MetaAtom implements MetaType {
	public enum BaseType implements MetaType {
		STRING, INT, DATE, FLOAT, CURRENCY, DATETIME, BOOLEAN, RELATION, UNKNOWN;

		public static BaseType lookup(String name) {
			BaseType result = null;
			for (BaseType bt : BaseType.values())
				if (bt.name().equals(name)) {
					result = bt;
					break;
				}

			return result;
		}

		@Override
		public String getName() {
			return name();
		}

		@Override
		public MetaElement getElementByName(String name) {
			return null;
		}

		@Override
		public List<MetaElement> getElements() {
			return new ArrayList<>();
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	}

	private BaseType baseType;
	private String name;
	private int length;
	private int fractionLength;
	private String pattern;
	private String format;
	private List<MetaEnumeration> enumerations;
	private boolean padding;

	public MetaAtom() {

	}

	public MetaAtom(BaseType baseType, String name, int length, String pattern, String format) {
		this.baseType = baseType;
		this.name = name;
		this.length = length;
		this.pattern = pattern;
		this.format = format;
	}

	public BaseType getBaseType() {
		return baseType;
	}

	public void setBaseType(BaseType baseType) {
		this.baseType = baseType;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public List<MetaEnumeration> getEnumerations() {
		if (null == this.enumerations)
			this.enumerations = new ArrayList<>();
		return this.enumerations;
	}

	public void setEnumerations(List<String[]> enumerations) {
		this.enumerations = new ArrayList<>();
		for (String[] e : enumerations) {
			if (e.length == 2) {
				this.enumerations.add(new MetaEnumeration(e[0], e[1]));
			}
		}
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setPadding(boolean padding) {
		this.padding = padding;
	}

	public boolean getPadding() {
		return this.padding;
	}

	public void setFractionLength(int fractionLength) {
		this.fractionLength = fractionLength;
	}

	public int getFractionLength() {
		return this.fractionLength;
	}

	@Override
	public MetaElement getElementByName(String name) {
		return null;
	}

	@Override
	public List<MetaElement> getElements() {
		List<MetaElement> result = new ArrayList<>();

		return result;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}
