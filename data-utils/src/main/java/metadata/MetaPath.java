package metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.DataUtil;
import data.EnumUtil;

public class MetaPath implements Cloneable {
	public enum Option {
		NOP,OPTION_PRINT_MULTIPLICITY
	}

	List<MetaElement> path;

	public void add(MetaElement el) {
		getPath().add(el);
	}

	public List<MetaElement> getPath() {
		if (null == path)
			path = new ArrayList<>();

		return path;
	}

	@Override
	public MetaPath clone() {
		MetaPath result = new MetaPath();

		for (MetaElement el : this.getPath())
			result.getPath().add(el);

		return result;
	}

	public int getMultiplicityCount() {

		int result = 0;
		for (MetaElement el : path) {
			if (el.getMaxAantal() > 1)
				++result;
		}

		return result;
	}

	public String print(Option... options) {
		String result = "";
		int i = 0;
		for (MetaElement el : path) {
			result = result + el.getName();
			if (el.getMaxAantal() > 1 && EnumUtil.getInstance().contains(options, Option.OPTION_PRINT_MULTIPLICITY)) {
				result += "[" + el.getMaxAantal() + "]";
			}
			if (++i < getPath().size())
				result += DataUtil.PATH_SEPARATOR;
		}

		return result;
	}

	public void reverse() {
		Collections.reverse(getPath());
	}

	public int size() {
		if (null == path)
			return 0;
		else
			return path.size();
	}

	public boolean isEmpty() {
		return null == path || path.isEmpty();
	}

	public MetaElement get(int n) {
		return getPath().get(n);
	}

}
