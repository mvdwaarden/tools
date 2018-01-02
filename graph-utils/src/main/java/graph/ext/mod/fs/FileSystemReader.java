package graph.ext.mod.fs;

import graph.GraphOption;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.fs.DirectoryMemberEdge;
import graph.ext.dm.fs.FileSystemNode;
import graph.ext.dm.fs.FileSystemNodeFactory;
import graph.util.GraphReader;

import java.io.File;
import java.io.FileFilter;

import data.EnumUtil;

/**
 * <pre>
 * Purpose: Converts file system directory information into a graph with
 * DirectoryNodes and FileNodes.
 * 
 * Currently file symbolic links could result in unexpected behaviour (e.g.not tested)
 * 
 * 2014-07-28: Adding Edges checks for duplicates, in large file graphs this
 *             will lead to considerable overhead. 
 *             
 *             For this a new constructor added, which allows duplicates
 * 
 * &#64;author mwa17610
 * </pre>
 */
public class FileSystemReader extends GraphReader<FileSystemNode, Edge<FileSystemNode>> {
	public enum Option {
		NOP, OPTION_RECURSIVE;
	}

	boolean allowDuplicates;

	public FileSystemReader(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}

	public FileSystemReader() {

	}

	public void read(File folder, Graph<FileSystemNode, Edge<FileSystemNode>> graph, Option... options) {
		read(folder, graph, "", options);
	}

	public void read(File folder, Graph<FileSystemNode, Edge<FileSystemNode>> graph, final String regex,
			Option... options) {
		FileSystemNodeFactory factory = new FileSystemNodeFactory(null);

		try {
			File[] files;

			if (null != regex && !regex.isEmpty())
				files = folder.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						boolean result = false;
						if (pathname.isDirectory())
							result = true;
						else if (pathname.getPath().matches(regex))
							result = true;

						return result;
					}
				});
			else
				files = folder.listFiles();

			FileSystemNode dn = factory.createNode(folder);
			if (null != files) {
				for (File f : files) {
					FileSystemNode node = factory.createNode(f);
					boolean recurse = EnumUtil.getInstance().contains(options, Option.OPTION_RECURSIVE)
							&& f.isDirectory() && !graph.getNodes().contains(node);
					// check for duplicates if they are not allowed
					if (!allowDuplicates)
						graph.addEdge(new DirectoryMemberEdge(dn, node), GraphOption.CHECK_DUPLICATES);
					else
						graph.addEdge(new DirectoryMemberEdge(dn, node));
					if (recurse)
						read(f, graph, regex, options);
				}
			}
		} finally {

		}
	}

	public Graph<FileSystemNode, Edge<FileSystemNode>> read(String folder) {
		return read(folder, "", Option.OPTION_RECURSIVE);
	}

	public Graph<FileSystemNode, Edge<FileSystemNode>> read(String folder, String regex) {
		return read(folder, regex, Option.OPTION_RECURSIVE);
	}

	public Graph<FileSystemNode, Edge<FileSystemNode>> read(String folder, String regex, Option... options) {
		Graph<FileSystemNode, Edge<FileSystemNode>> graph = new Graph<>();

		graph.setName(folder);
		File f = new File(folder);
		read(f, graph, regex, options);

		return graph;
	}
}
