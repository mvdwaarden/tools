package jee.jndi;

public class WebLogicNamingContext extends NamingContext {
	private String host;
	private int port;

	public WebLogicNamingContext(String host, int port, String user, String password) {
		super("weblogic.jndi.WLInitialContextFactory", "t3://" + host + ((port >=0) ? ":" + port : ""), user, password);
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
