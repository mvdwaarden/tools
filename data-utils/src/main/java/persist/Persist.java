package persist;

/**
 * Purpose: Interface for storing and reading information.
 * 
 * @author mwa17610
 * 
 */
public interface Persist<T, ID> {
	PersistId<ID> save(Connection conn, T object, boolean createTransaction);

	T read(Connection conn, Class<T> cls, PersistId<ID> id, boolean createTransaction);
}
