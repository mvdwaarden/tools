package graph.dm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import data.LogUtil;
import object.ObjectIterator;

/**
 * Purpose: Represents a node in a graph.
 * 
 * @author mwa17610
 * 
 */
public class Node implements NameDescription, UniqueId {
	private String id;
	private String name;
	private String description;

	public Node() {
		super();
	}

	public Node(String id) {
		super();
		this.id = id;
	}

	public Node(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public Node(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getName() {
		// return (null != name) ? name : "" + hashCode();
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
		// return (null != description) ? description : "" + hashCode();
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		String result = getClass().getSimpleName();

		return result;

	}

	public String getUniqueId() {
		String result;
		if (null == getId())
			result = getClass().getName() + "_" + hashCode();
		else
			result = getClass().getSimpleName() + ":" + getId();

		return result;
	}

	public boolean treatAsSame(Node node) {
		try {
			if (node == this || (node.getClass() == getClass() && node.getId() != null && node.getId().equals(getId())))
				return true;
			else
				return false;
		} catch (Exception e) {
			LogUtil.getInstance().ignore("problem comparing nodes", e);
			return false;
		}
	}

	public String getProperty(String property) {
		String result = "";
		Field field = ObjectIterator.lookupFields(Node.class, getClass()).get(property);
		if (null != field) {
			try {
				Object value = field.get(this);
				result = (null != value) ? String.valueOf(value) : null;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogUtil.getInstance()
						.error("problem getting property [" + property + "] from [" + getClass().getName() + "]", e);
			}
		} else {
			Method method = ObjectIterator.lookupMethods(Node.class, getClass()).get(property);
			if (null != method) {
				try {
					Object value = method.invoke(this);
					result = (null != value) ? String.valueOf(value) : null;
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					LogUtil.getInstance().error(
							"problem getting property by method [" + property + "] from [" + getClass().getName() + "]",
							e);
				}
			}
		}
		return result;
	}
}
