package graph.ext.dm.fs;

import graph.dm.Edge;

/**
 * Purpose: Defines a connection between a two file system nodes. Most likely a
 * DirectoryNode and a FileNode.
 * 
 * @author mwa17610
 * 
 */
public class DirectoryMemberEdge extends Edge<FileSystemNode> {
	public DirectoryMemberEdge(FileSystemNode source, FileSystemNode target) {
		super(source, target);
		setName("has");
	}
}
