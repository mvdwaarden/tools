package jee.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import data.LogUtil;

public class NamingContext {
	private String factory;
	private String url;
	private String user;
	private String password;
	private Context ctx;

	public NamingContext(String factory, String url, String user, String password) {
		this.factory = factory;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getFactory() {
		return factory;
	}

	public void setFactory(String factory) {
		this.factory = factory;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Context getContext() {
		return ctx;
	}

	protected boolean connect(String factory, String url, String user, String password) {

		this.factory = factory;
		this.url = url;
		this.user = user;
		this.password = password;
		return connect();
	}

	public boolean isConnected() {
		return null != ctx;
	}

	public boolean connect() {
		boolean result = false;

		if (!isConnected()) {
			Hashtable<String, Object> properties = new Hashtable<String, Object>();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, factory);
			properties.put(Context.PROVIDER_URL, url);
			properties.put(Context.SECURITY_PRINCIPAL, user);
			properties.put(Context.SECURITY_CREDENTIALS, password);
			try {
				ctx = new InitialContext(properties);
				result = true;
			} catch (NamingException e) {
				LogUtil.getInstance().error("unable to create naming context", e);

			}
		} else
			result = true;

		return result;
	}

	public void disconnect() {
		if (null != ctx)
			try {
				ctx.close();
			} catch (NamingException e) {
				LogUtil.getInstance().error("unable to create naming context", e);
			} finally {
				ctx = null;
			}
	}

}
