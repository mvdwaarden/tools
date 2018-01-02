package metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping from meta elements to interface codes
 * 
 * @author mwa17610
 *
 */
public class InterfaceMapping {
	Map<MetaElement, List<Integer>> interfaceMap;

	public List<Integer> getMappingsByElement(MetaElement el) {
		return getInterfaceMap().get(el);
	}

	public void add(MetaElement el, List<Integer> newMappings) {
		List<Integer> mappings = getMappingsByElement(el);

		if (null == mappings) {
			mappings = new ArrayList<>();
			getInterfaceMap().put(el, mappings);
		}
		for (Integer code : newMappings) {
			if (!mappings.contains(code)) {
				mappings.add(code);
			}
		}
	}

	public Map<MetaElement, List<Integer>> getInterfaceMap() {
		if (null == interfaceMap)
			interfaceMap = new HashMap<>();

		return interfaceMap;
	}

	public String getMappingsLabelByElement(MetaElement el) {
		List<Integer> ifCodes = getMappingsByElement(el);
		String result = "";
		if (null != ifCodes) {
			for (Integer ifCode : ifCodes) {
				if (!result.isEmpty())
					result += " ";
				result += ifCode;
			}
		}

		return result;
	}
}
