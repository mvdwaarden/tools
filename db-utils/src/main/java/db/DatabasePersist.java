package db;

import persist.Connection;
import persist.Persist;
import persist.PersistId;

public class DatabasePersist<T> implements Persist<T, Long> {

	@Override
	public PersistId<Long> save(Connection conn, Object object, boolean createTransaction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T read(Connection conn, Class<T> cls, PersistId<Long> id, boolean createTransaction) {
		// TODO Auto-generated method stub
		return null;
	}

}
