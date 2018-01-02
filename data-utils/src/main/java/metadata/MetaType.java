package metadata;

import java.util.List;

public interface MetaType {
	String getName();

	MetaElement getElementByName(String name);

	List<MetaElement> getElements();

	boolean isEmpty();
}
