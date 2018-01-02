package graph.parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import data.DataUtil;
import data.Filter;
import graph.dm.Edge;
import graph.dm.Node;

/**
 * Paradigm
 * 
 * <pre>
 * SAX Parsing 
 * - Calls startElement for an opening tag 
 * - Calls endElement for a closing tag
 * 
 * The SAXHandler 
 * - Assumes data is contained between opening and closing tags 
 * - Creates an entry on the stack for each element encountered during SAX processing
 * 
 * So: 
 * - startElement createsNodes (to also allow access to the attributes) 
 * - endElement allows for postNodeCreation
 * 
 * This allows for: 
 * - Choose when to create nodes 
 * - Fold XML element tags
 * 
 * The advantage of this class is that there is no XSD required, and it allows
 * for more 'flexible' parsing of XML.
 * 
 * Of course also a mayor benefit is gained when extracting small pieces of information from
 * humongous files.
 * 
 * </pre>
 * 
 * @author mwa17610
 * 
 */
public abstract class GraphHandlerFactory<N extends Node, E extends Edge<N>>
		implements NodeFactory<N>, EdgeFactory<N, E> {
	GraphSAXHandler<N, E> handler;

	public GraphHandlerFactory(GraphSAXHandler<N, E> handler) {
		super();
		this.handler = handler;
	}

	public abstract N createNode(String uri, String localName, String qName, Attributes atts);

	public abstract List<E> createEdges(N source, N target);

	public abstract void postNodeCreation(N currentNode, String uri, String localName, String qName,
			StringBuilder data);

	public void setQualifiedName(N node, Attributes atts) {
		setNodeValuesIfEmpty(node, atts);
		node.setId(expandPrefix(node.getId()));
	}

	public void setNodeValuesIfEmpty(N node, Attributes atts) {
		setNodeValuesIfEmpty(node, atts, "Name", true);
	}

	public void setNodeValuesIfEmpty(N node, String id) {
		if (null == node.getId())
			node.setId(id);
		if (null == node.getName())
			node.setName(id);
		if (null == node.getDescription())
			node.setDescription(id);
	}

	public String getRelativeId() {
		String result = DataUtil.getInstance().getRelativename(handler.getBaseUrl(), getSourceUrl());

		return result;
	}

	public String getRelativePath() {
		String result = DataUtil.getInstance().getFoldername(getRelativeId());

		return result;
	}

	public void setNodeValuesIfEmpty(N node, Attributes atts, String attName, boolean ignoreCase) {
		String id = atts.getValue(attName);
		if (null == id) {
			String tmp = attName.toLowerCase();
			for (int i = 0; i < atts.getLength(); ++i) {
				if (atts.getLocalName(i).toLowerCase().equals(tmp)) {
					id = atts.getValue(i);
					break;
				}
			}
		}
		// try other ids (1) the id as defined by the 'name' attribute, (2) the
		// relative name,
		String[] tryoutIds = new String[] { id, getRelativeId(), Integer.toString(node.hashCode()) };
		for (String tryoutId : tryoutIds) {
			if (null != tryoutId) {
				setNodeValuesIfEmpty(node, tryoutId);
				break;
			}
		}
	}

	public List<N> filterNodesToRoot(Filter<N> filter) {
		return handler.filterNodesToRoot(filter);
	}

	public String expandPrefix(String value) {
		return handler.expandPrefix(value);
	}

	public boolean pathTest(String pathTest) {
		return handler.pathTest(pathTest);
	}

	public String getPath() {
		return handler.getPath();
	}

	/**
	 * 
	 * @param qName
	 *            the qualified name provided by SAX
	 * @param uriTest
	 *            the URI to test
	 * @param localNameTest
	 *            the local name to test
	 * @param uri
	 *            the URI provided by SAX
	 * @param localName
	 *            the local name provided by SAX
	 * @return
	 */
	public boolean nodeTest(String qName, String uriTest, String localNameTest, String uri, String localName) {
		return handler.nodeTest(qName, uriTest, localNameTest, uri, localName);
	}

	public N getCurrentNode() {
		return handler.getCurrentNode();
	}

	public N getRootNode() {
		return handler.getRootNode();
	}

	public N getParentNode(Class<?> cls) {
		return handler.getParentNode(cls);
	}

	public String getSourceUrl() {
		return handler.getSourceUrl();
	}

	public String getBaseUrl() {
		return handler.getBaseUrl();
	}

	public String getXsiType(Attributes atts) {
		return handler.getXsiType(atts);
	}

	public String getExpandprefix(String str) {
		return handler.expandPrefix(str);
	}

	public List<Edge<N>> createList(E[] items) {
		List<Edge<N>> result = new ArrayList<>();

		for (E item : items)
			result.add(item);

		return result;
	}
}
