package nl.ordina.tools.sql;

public class SQLUtil {
	public enum Dialect {
		ORACLE, MICROSOFT
	}

	private static final ThreadLocal<SQLUtil> instance = new ThreadLocal<SQLUtil>();

	/**
	 * Get the thread local singleton
	 * 
	 * @return
	 */
	public static SQLUtil getInstance() {
		SQLUtil result = instance.get();

		if (null == result) {
			result = new SQLUtil();
			instance.set(result);
		}

		return result;
	}

	public SQLParser getSQLParser(Dialect dialect){
		SQLParser result = null;
		switch (dialect){
		case ORACLE :
			result = new OracleSQLParser();
			break;
		case MICROSOFT: 
			result = new MicrosoftSQLParser();
			break;
		}
		return result;
	}

}
