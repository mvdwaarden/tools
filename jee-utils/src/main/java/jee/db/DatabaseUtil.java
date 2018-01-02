package jee.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import data.LogUtil;
import data.Util;

public class DatabaseUtil implements Util {
	private InitialContext ctx;
	private static ThreadLocal<DatabaseUtil> instance = new ThreadLocal<DatabaseUtil>();

	public static DatabaseUtil getInstance() {
		DatabaseUtil result = instance.get();
		if (null == result) {
			result = new DatabaseUtil();
			instance.set(result);
		}
		return result;
	}

	public Connection getConnection(String jndiName, String user, String password) {
		Connection result = null;

		try {
			if (null == ctx)
				ctx = new InitialContext();
			Object obj = ctx.lookup(jndiName);
			if (obj instanceof DataSource) {
				DataSource ds = (DataSource) obj;
				result = ds.getConnection(user, password);
			}
		} catch (NamingException | SQLException e) {
			LogUtil.getInstance().error("problem getting connection [" + jndiName + "]");
		}
		return result;
	}
}
