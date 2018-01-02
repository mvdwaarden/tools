package metadata;

import java.util.ArrayList;
import java.util.List;

public class MetaComposite implements MetaType {
	private String name;
	private List<MetaElement> elements;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<MetaElement> getElements() {
		if (null == elements)
			elements = new ArrayList<>();
		
		return elements;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public MetaElement getElementByName(String name) {
		MetaElement result = null;
		if (null != elements) {
			for (MetaElement el : elements) {
				if (null != el.getName() && el.getName().equals(name)) {
					result = el;
					break;
				}
			}
		}

		return result;
	}

	public void removeElementByName(String name) {
		MetaElement toRemove = getElementByName(name);
		removeElement(toRemove);
	}

	public void removeElement(MetaElement el) {
		if (null != elements)
			elements.remove(el);
	}

	public void addElement(MetaElement el) {
		getElements().add(el);
	}
}
