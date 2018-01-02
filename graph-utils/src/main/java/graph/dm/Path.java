package graph.dm;

/**
 * Purpose: Specifies the path interface. In a graph a path is typically unique
 * for all nodes of a certain class.
 * 
 * @author mwa17610
 * 
 */
public interface Path {
	void setPath(String path);

	String getPath();
}
