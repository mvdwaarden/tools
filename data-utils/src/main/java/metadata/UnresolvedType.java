package metadata;

import java.util.ArrayList;
import java.util.List;

public class UnresolvedType implements MetaType {
	private String name;

	public UnresolvedType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
