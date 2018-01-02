package metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import metadata.MetaAtom.BaseType;
import data.DataUtil;
import data.LogUtil;

/*
 * 
 */
public class MetaElement {
	private int id;
	private String name;
	private String localname;
	private MetaElement parent;
	private String tag;
	private boolean mandatory;
	private int maxAantal;
	private MetaType type;
	private String[] unique;
	private List<String> documentation;
	private String text;
	private MetaInterval interval;
	private List<MetaEnumeration> enumerations;
	{
		maxAantal = 1;
		id = -1; // invalid
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public int getMaxAantal() {
		return maxAantal;
	}

	public void setMaxAantal(int maxAantal) {
		this.maxAantal = maxAantal;
	}

	public MetaElement getParent() {
		return parent;
	}

	public void setParent(MetaElement parent) {
		this.parent = parent;
	}

	public int getId() {
		if (id < 0)
			id = DataUtil.getInstance().getUuid();

		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
		if (null == localname)
			this.localname = name;
	}

	public String getName() {
		return name;
	}

	public String getLocalname() {
		return localname;
	}

	public void setLocalname(String localname) {
		this.localname = localname;
		if (null == name)
			this.name = localname;
	}

	@SuppressWarnings("unchecked")
	public <T extends MetaType> T getType() {
		return (T) this.type;
	}

	public void setType(MetaType type) {
		this.type = type;
	}

	public void setType(String name) {
		this.type = new UnresolvedType(name);
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public final String[] getUnique() {
		if (null != unique)
			return Arrays.copyOf(unique, unique.length);
		else
			return new String[0];
	}

	public void setUnique(String[] unique) {
		this.unique = Arrays.copyOf(unique, unique.length);
	}

	public List<MetaElement> getUniqueElements() {
		List<MetaElement> result = new ArrayList<>();
		if (null != getUnique() && type instanceof MetaComposite)
			for (String unique : getUnique()) {
				MetaElement el = type.getElementByName(unique);
				if (null != el)
					result.add(el);
				else
					LogUtil.getInstance().warning("element not found [" + unique + "]");
			}

		return result;
	}

	/** Update unique value */
	public void updateUnique() {
		// Remove invalid keys
		List<MetaElement> uniqueElements = getUniqueElements();

		// Rebuild the unique string
		String[] uniqueUpdate = new String[uniqueElements.size()];
		int idx = 0;
		for (MetaElement el : uniqueElements) {
			uniqueUpdate[idx++] = el.getName();
		}
		setUnique(uniqueUpdate);
	}

	public void addDocumentation(String documentation) {
		getDocumentation().add(documentation);
	}

	public List<String> getDocumentation() {
		if (null == documentation)
			this.documentation = new ArrayList<>();
		return documentation;
	}

	public void setEnumerations(List<MetaEnumeration> enumerations) {
		if (!enumerations.isEmpty()) {
			this.enumerations = enumerations;
			boolean fix = false;
			for (MetaEnumeration enumeration : enumerations) {
				if (null == enumeration.getValue()) {
					enumeration.setValue(enumeration.getCode());
					fix = true;
				}
			}
			if (fix)
				LogUtil.getInstance().warning(
						"enumeration fix on element [" + getName() + "] , value is not defined for element");
		} else
			this.enumerations = enumerations;
	}

	public List<MetaEnumeration> getEnumerations() {
		List<MetaEnumeration> result = enumerations;

		// Look if there are enumerations for the type, only use these if they
		// are defined
		if ((null == result || result.isEmpty()) && getType() instanceof MetaAtom) {
			List<MetaEnumeration> tmp = this.<MetaAtom> getType().getEnumerations();
			if (null != tmp) {
				enumerations = result = tmp;
			}
		}

		return result;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Verifies if this element is a parent of the passed element
	 * 
	 * @param el
	 * @return
	 */
	public boolean isParentOf(MetaElement el) {
		boolean result = false;
		MetaElement parent = el.getParent();

		do {
			if (parent == this) {
				result = true;
				break;
			}
			if (null != parent)
				parent = parent.getParent();
		} while (null != parent);

		return result;
	}

	public MetaInterval getInterval() {
		return interval;
	}

	public void setInterval(MetaInterval interval) {
		if (getType() instanceof MetaAtom && this.<MetaAtom> getType().getBaseType() != BaseType.DATE
				&& this.<MetaAtom> getType().getBaseType() != BaseType.DATETIME)
			this.interval = interval;

	}

	/**
	 * Create a shallow copy of this element
	 * 
	 * @return
	 */
	public MetaElement shallowCopy() {
		MetaElement result = new MetaElement();

		result.maxAantal = maxAantal;
		result.enumerations = result.enumerations;
		result.localname = localname;
		result.parent = parent;
		result.unique = unique;
		result.text = text;
		result.tag = tag;
		result.type = type;
		result.interval = interval;
		result.id = id;
		result.documentation = documentation;

		return result;
	}

}
