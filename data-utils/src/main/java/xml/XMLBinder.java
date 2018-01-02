package xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import xml.XMLBinding.BindingValueType;

public class XMLBinder {
	private List<XMLBinding> bindings;

	public XMLBinder() {

	}

	public XMLBinder(InputStream is) {
		this(new XMLBinding[] { new XMLBinding("param0", is) });
	}

	public XMLBinder(String str) {
		this(new XMLBinding[] { new XMLBinding("param0", str) });
	}

	public XMLBinder(XMLBinding[] binds) {
		for (XMLBinding bind : binds) {
			getBindings().add(bind);
		}
	}

	public List<XMLBinding> getBindings() {
		if (null == bindings)
			bindings = new ArrayList<>();

		return bindings;
	}

	@SuppressWarnings("unchecked")
	public <T> T getFirstBindingByType(BindingValueType type) {
		T result = null;
		Optional<XMLBinding> opt = getBindings().stream().filter(b -> b.getValueType() == type).findFirst();
		if (opt.isPresent())
			result = (T) opt.get().getValue();
		return result;
	}

	public boolean close() {
		boolean result = false;

		for (XMLBinding bind : getBindings()) {
			boolean tmp = bind.close();

			if (result == false)
				result = tmp;
		}

		return result;
	}
}
