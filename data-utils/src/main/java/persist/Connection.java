package persist;

import java.io.Closeable;

/**
 * Purpose: Interface for connecting to a graph store.
 * 
 * @author mwa17610
 * 
 */
public interface Connection extends Closeable {
	boolean connect();

	void disconnect();
}
