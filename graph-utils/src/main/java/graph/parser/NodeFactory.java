package graph.parser;

import org.xml.sax.Attributes;

import graph.dm.Node;

public interface NodeFactory<N extends Node> {
	N createNode(String uri, String localName, String qName, Attributes atts);
}
