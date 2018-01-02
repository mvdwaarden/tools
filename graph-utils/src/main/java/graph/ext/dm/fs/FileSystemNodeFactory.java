package graph.ext.dm.fs;

import java.io.File;
import java.util.List;

import org.xml.sax.Attributes;

import graph.dm.Edge;
import graph.dm.EdgeType;
import graph.parser.GraphHandlerFactory;
import graph.parser.GraphSAXHandler;

/**
 * Purpose: Factory for creating FileSystemNodes during SAX parsing.
 * 
 * @author mwa17610
 * 
 */
public class FileSystemNodeFactory extends GraphHandlerFactory<FileSystemNode, Edge<FileSystemNode>> {
	public FileSystemNodeFactory(GraphSAXHandler<FileSystemNode, Edge<FileSystemNode>> handler) {
		super(handler);
	}

	public FileSystemNode createNode(File file) {
		FileSystemNode node = null;
		if (file.isDirectory()) {
			node = new DirectoryNode();
			node.setName(file.getName());
			node.setId(file.getPath());
		} else {
			node = new FileNode();
			node.setName(file.getName());
			node.setId(file.getPath());
		}

		return node;
	}

	@Override
	public FileSystemNode createNode(String uri, String localName, String qName, Attributes atts) {
		return null;
	}

	@Override
	public void postNodeCreation(FileSystemNode currentNode, String uri, String localName, String qName,
			StringBuilder data) {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Edge<FileSystemNode>> createEdges(FileSystemNode source, FileSystemNode target) {
		return createList(new Edge[]{new Edge<>(source,target,EdgeType.HAS)});
	}
}
