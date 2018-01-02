package graph.io;

import graph.dm.Node;
import graph.util.GraphUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import data.StringUtil;

/**
 * Purpose simple JSON output methods for graph related objects.
 * 
 * @author mwa17610
 * 
 */
public class SimpleJSONWriter {
	/**
	 * Prints a list of nodes as a JSON string
	 * 
	 * @param nodes
	 * @param baseClass
	 * @return
	 */
	public String print(List<Node> nodes, Class<?> baseClass) {
		StringBuilder result = new StringBuilder();
		result.append("[");
		for (Node n : nodes) {
			result.append("{");
			print(n, baseClass, result);
			result.append("},");
		}
		StringUtil.getInstance().stripEnd(result, ",");
		result.append("]");
		return result.toString();
	}

	/**
	 * Print a Node as a JSON string
	 * 
	 * @param node
	 * @param baseClass
	 * @param buf
	 * @return
	 */
	public String print(Node node, Class<?> baseClass, StringBuilder buf) {
		StringBuilder result;

		if (null == buf)
			result = new StringBuilder();
		else
			result = buf;
		Map<String, Object> nvps = GraphUtil.getInstance().object2NV(node, baseClass);

		result.append(node.getClass().getSimpleName());
		result.append(":");
		result.append("{");
		for (Entry<String, Object> nvp : nvps.entrySet()) {
			result.append(nvp.getKey());
			result.append(":");
			if (null != nvp.getValue())
				print(nvp.getValue(), baseClass, result);
			else
				result.append("null");

			result.append(",");

		}
		StringUtil.getInstance().stripEnd(result, ",");
		result.append("}");

		return result.toString();

	}

	/**
	 * Method which prints a list as a JSON string
	 * 
	 * @param list
	 * @param baseClass
	 * @param buf
	 */
	private void print(List<?> list, Class<?> baseClass, StringBuilder buf) {
		buf.append("[");
		for (Object obj : list) {
			print(obj, baseClass, buf);
			buf.append(",");
		}
		StringUtil.getInstance().stripEnd(buf, ",");
		buf.append("]");
	}

	/**
	 * Method which prints an object as a JSON string
	 * 
	 * @param list
	 * @param baseClass
	 * @param buf
	 */

	@SuppressWarnings("rawtypes")
	private void print(Object obj, Class<?> baseClass, StringBuilder buf) {
		if (obj instanceof Node)
			print((Node) obj, baseClass, buf);
		else if (obj instanceof List)
			print((List) obj, baseClass, buf);
		else if (obj instanceof String)
			buf.append("\"" + (String) obj + "\"");
		else
			buf.append(String.valueOf(obj));

	}
}
