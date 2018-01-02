package metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import metadata.MetaAtom.BaseType;
import data.LogUtil;

/**
 * MetaData for hierarchical structures
 * 
 * @author mwa17610
 * 
 */
public class MetaData {
	public static final String COMPLEX_TYPE_NAME = "_interal_REF";
	public static final String DEFAULT_TYPE_NAME = "_interal_STRING";
	private static MetaAtom DEFAULT_TYPE = new MetaAtom(BaseType.STRING, DEFAULT_TYPE_NAME, 255, null, null);
	private String targetNamespace;
	private String rootTag;
	private Map<String, MetaElement> roots = new LinkedHashMap<>();
	private Map<String, MetaType> types = new HashMap<>();
	private Map<String, MetaRelation> relations = new HashMap<>();
	private Map<String, String> aliasToPublicname = new HashMap<>();

	{
		types.put(DEFAULT_TYPE_NAME, DEFAULT_TYPE);
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getRootTag() {
		return rootTag;
	}

	public void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}

	public void addRoot(MetaElement root) {
		if (null != root) {
			if (null == roots.get(root.getName()))
				roots.put(root.getName(), root);
			else
				LogUtil.getInstance().warning(
						"did not add element [" + root.getName() + "] because it already exists in the metadata");
		}
	}

	protected void removeRoot(MetaElement root) {
		if (null != root) {
			if (null != roots.get(root.getName()))
				roots.remove(root.getName());
			else
				LogUtil.getInstance().info("did not find element [" + root.getName() + "] nothing removed");
		}
	}

	public List<MetaElement> getElementsById(int id) {
		List<MetaElement> result = new ArrayList<>();

		for (Entry<String, MetaElement> e : roots.entrySet()) {
			if (e.getValue().getId() == id)
				result.add(e.getValue());
		}

		return result;
	}

	public List<MetaElement> getRoots() {
		List<MetaElement> result = new ArrayList<>();

		for (Entry<String, MetaElement> e : roots.entrySet()) {
			result.add(e.getValue());
		}

		return result;

	}

	public MetaElement getRootByName(String name) {
		return roots.get(name);
	}

	public int size() {
		return roots.size();
	}

	public List<MetaElement> getMetaElements() {
		return findElement(getRoots(), filter -> true, false);
	}

	private List<MetaElement> findElement(Collection<MetaElement> elements, MetaDataElementFilter filter, boolean one) {
		List<MetaElement> result = new ArrayList<>();

		if (one) {
			Optional<MetaElement> optResult = elements.stream().filter(el -> filter.include(el)).findFirst();

			if (optResult.isPresent())
				result.add(optResult.get());
		} else
			result = elements.stream().filter(el -> filter.include(el)).collect(Collectors.toList());

		if (!one || result.isEmpty()) {
			for (MetaElement el : elements) {
				List<MetaElement> childs = el.getType().getElements();
				if (!childs.isEmpty())
					result.addAll(findElement(childs, filter, one));
				if (one && !result.isEmpty())
					break;
			}
		}

		return result;
	}

	/* Cache */
	Map<String, List<MetaElement>> cacheMetaElementListByName = new HashMap<>();

	public List<MetaElement> findElementsByName(final String name) {
		final String publicname = getPublicnameByAlias(name);

		List<MetaElement> result = cacheMetaElementListByName.get(name);
		if (null == result) {
			result = findElement(roots.values(), el -> el.getName().equalsIgnoreCase(name)
					|| (null != publicname && el.getName().equalsIgnoreCase(publicname)), true);
			if (null != result)
				cacheMetaElementListByName.put(name, result);
		}
		return result;
	}

	public List<MetaElement> findElementsById(final int id) {
		return findElement(roots.values(), el -> el.getId() == id, false);
	}

	public List<MetaElement> findElementsByType(final MetaType type) {
		return findElement(roots.values(), el -> el.getType().equals(type), false);
	}

	public List<MetaPath> getPathToRoot(int id) {
		List<MetaPath> result = new ArrayList<>();

		for (MetaElement el : findElementsById(id))
			result.add(getPathToRoot(el));

		return result;
	}

	public MetaPath getPathToRoot(MetaElement el) {
		MetaPath result = new MetaPath();

		while (null != el) {
			result.add(el);
			el = el.getParent();
		}
		result.reverse();

		return result;
	}

	public List<MetaElement> getChilds(MetaElement parent) {
		List<MetaElement> result = new ArrayList<>();

		for (MetaElement el : parent.getType().getElements())
			result.add(el);

		return result;
	}

	public void addType(MetaType type) {
		if (null == types.get(type.getName())) {
			types.put(type.getName(), type);
		}
	}

	public List<MetaType> getTypes() {
		return new ArrayList<>(types.values());
	}

	public void resolveTypes() {
		resolveTypes(roots.values());
	}

	private void resolveTypes(Collection<MetaElement> elements) {
		for (MetaElement el : elements) {
			if (el.getType() instanceof UnresolvedType) {
				MetaType type = getTypeByName(el.getType().getName());
				if (null != type)
					el.setType(type);
			}
			if (el.getType() instanceof MetaComposite)
				resolveTypes(el.<MetaComposite> getType().getElements());
		}
	}

	public <T extends MetaType> T getTypeByName(String name) {
		@SuppressWarnings("unchecked")
		T result = (T) types.get(name);

		return result;
	}

	public List<MetaPath> getMultiplicityPaths() {
		return getMultiplicityPaths(roots.values(), new MetaPath());
	}

	private List<MetaPath> getMultiplicityPaths(Collection<MetaElement> elements, MetaPath path) {
		List<MetaPath> result = new ArrayList<>();

		for (MetaElement el : elements) {
			MetaPath p = path.clone();
			p.add(el);
			if (el.getMaxAantal() > 1) {
				result.add(p);
			} else {
				List<MetaPath> mpaths = getMultiplicityPaths(el.getType().getElements(), p);

				for (MetaPath mp : mpaths) {
					result.add(mp);
				}
			}
		}

		return result;
	}

	public void updateParentChildRelations() {
		roots.values().stream().forEach(el -> updateParentChildRelations(el));
	}

	private void updateParentChildRelations(MetaElement element) {
		element.getType().getElements().forEach(child -> {
			child.setParent(element);
			updateParentChildRelations(child);
		});
	}

	public void moveElement(MetaElement el, MetaElement target) {
		if (null == el.getParent()) {
			removeRoot(el);
		} else {
			el.getParent().<MetaComposite> getType().removeElement(el);
			el.getParent().updateUnique();
		}
		if (null == target) {
			addRoot(el);
			el.setParent(null);
		} else {
			target.<MetaComposite> getType().addElement(el);
			el.setParent(target);
		}

	}

	public boolean isAlias(String alias) {
		boolean result;
		String publicname = getPublicnameByAlias(alias);

		if (null != publicname && !publicname.equalsIgnoreCase(alias))
			result = true;
		else
			result = false;

		return result;
	}

	public void addAlias(String publicname, String alias) {
		if (!alias.equalsIgnoreCase(publicname))
			aliasToPublicname.put(alias.toLowerCase(), publicname);
		else
			LogUtil.getInstance().warning("tried to insert alias which is the same as the public name");
	}

	public String getPublicnameByAlias(String alias) {
		String result = null;

		String tmp = aliasToPublicname.get(alias.toLowerCase());

		while (null != tmp) {
			result = tmp;
			tmp = aliasToPublicname.get(tmp.toLowerCase());
		}

		return result;

	}

	public boolean metaElementsShareCommonParent(MetaElement el1, MetaElement el2) {
		MetaPath pathEl1 = getPathToRoot(el1);
		MetaPath pathEl2 = getPathToRoot(el2);

		return pathEl1.get(0).equals(pathEl2.get(0));
	}

	public void addRelation(MetaRelation rel) {
		relations.put(rel.getName(), rel);
	}

	public List<MetaRelation> getRelations() {
		return new ArrayList<>(relations.values());
	}
}
