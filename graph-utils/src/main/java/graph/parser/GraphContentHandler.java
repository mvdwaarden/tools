package graph.parser;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import graph.dm.Edge;
import graph.dm.Graph;
import graph.dm.Node;

public interface GraphContentHandler<N extends Node, E extends Edge<N>> extends ContentHandler {

	void setSourceUrl(String url);

	void setBaseUrl(String baseUrl);

	N getFirstNode();

	Graph<N, E> getGraph();

	void setXMLReader(XMLReader xmlReader);

	void setInputSource(InputSource is);

	public <T> T unmarshal(Class<?> cls);

	void setConfigurationFilename(String ignorefile);

	String getConfigurationFilename();
}
