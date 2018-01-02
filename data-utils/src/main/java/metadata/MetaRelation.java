package metadata;

import java.util.ArrayList;
import java.util.List;

import metadata.MetaAtom.BaseType;

/**
 * Reference
 * 
 * @author mwa17610
 *
 */
public class MetaRelation extends MetaElement {
	/*
	 * containment -> true, only 1 source, containment false -> multiple sources
	 */
	boolean containment;
	private List<MetaElement> sources;
	private MetaElement target;

	public MetaRelation(String name) {
		this.setType(BaseType.RELATION);
		setName(name);
	}

	public MetaRelation(String name, MetaElement target) {
		this(name);
		setTarget(target);
	}

	public void setTarget(MetaElement target) {
		this.target = target;
	}

	public MetaElement getTarget() {
		return target;
	}

	public List<MetaElement> getSources() {
		if (null == sources)
			sources = new ArrayList<>();
		return sources;
	}

	public void addSource(MetaElement source) {
		getSources().add(source);
	}

	public boolean isContainment() {
		return containment;
	}

	public void setContainment(boolean containment) {
		this.containment = containment;
	}
}
