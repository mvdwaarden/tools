package db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import data.ConfigurationUtil;
import data.LogUtil;
import object.ObjectFactory;

public class DatabaseConnection implements persist.Connection {
	private static final String DB = "database.";
	private static final String DRIVER = ".driver";
	private static final String URL = ".url";
	private static final String AUTOCOMMIT = ".autocommit";
	private static final String USERNAME = ".username";
	private static final String PASSWORD = ".password";
	private String configuration;
	private String username;
	private String password;
	private Connection connection;

	public DatabaseConnection(String configuration) {
		this(configuration, ConfigurationUtil.getInstance().getSetting(DB + configuration + USERNAME),
				ConfigurationUtil.getInstance().getSetting(DB + configuration + PASSWORD));
	}

	public DatabaseConnection(String configuration, String username, String password) {
		this.configuration = configuration;
		this.username = username;
		this.password = password;
	}

	private boolean isAutoCommit() {
		return ConfigurationUtil.getInstance().getBooleanSetting(DB + configuration + AUTOCOMMIT, false);
	}

	/**
	 * Get JDBC driver
	 * 
	 * @param driver
	 *            JDBC driver class
	 */
	public Driver getDriver(String driver) {
		Driver result = null;
		// find driver
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver drv = drivers.nextElement();
			if (drv.getClass().getName().equalsIgnoreCase(driver)) {
				result = drv;
				break;
			}
		}
		// create one, if not found
		if (null == result) {
			try {
				ObjectFactory factory = ObjectFactory.getInstance();
				result = factory.<Driver>createObject(driver);
				DriverManager.registerDriver(result);
			} catch (Exception e) {
				LogUtil.getInstance()
						.warning("driver specified [" + driver + "] but could not be loaded, check classpath", e);

			}
		}
		return result;

	}

	@Override
	public boolean connect() {
		if (null == connection) {
			String url = ConfigurationUtil.getInstance().getSetting(DB + configuration + URL);
			try {
				Driver driver = getDriver(ConfigurationUtil.getInstance().getSetting(DB + configuration + DRIVER));
				Properties props = new Properties();
				props.put("user", username);
				props.put("password", password);
				connection = driver.connect(url, props);
				connection.setAutoCommit(isAutoCommit());
			} catch (Exception ex) {
				LogUtil.getInstance()
						.error("unable to get connection for [" + url + "], check the JDBC connection string", ex);
			}
		}
		return null != connection;
	}

	protected boolean close(Connection connection) {
		boolean result = false;
		try {
			if (null != connection) {
				if (!isAutoCommit())
					connection.rollback();
				connection.close();
			}
		} catch (SQLException e) {
			LogUtil.getInstance().error("Unable to close the transaction", e);
		}
		return result;
	}

	@Override
	public void close() {
		if (null != connection) {
			try {
				close(connection);
			} finally {
				connection = null;
			}
		}
	}

	@Override
	public void disconnect() {
		close();
	}
}
