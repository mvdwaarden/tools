package jee.jms;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.NamingException;

import data.DataUtil;
import data.LogUtil;
import jee.jndi.NamingContext;
import jee.jndi.WebLogicNamingContext;

public class JMSConnection implements persist.Connection {
	private NamingContext ctx;
	private String factoryName;
	private String queueName;
	private Connection connection;
	private Destination queue;
	private QueueSession session;

	public JMSConnection(NamingContext ctx, String factoryName) {
		this.ctx = ctx;
		this.factoryName = factoryName;
	}

	public boolean connect() {
		boolean result = false;

		if (!isConnected()) {
			Object jndiObject;
			try {
				ctx.connect();
				jndiObject = ctx.getContext().lookup(factoryName);

				if (jndiObject instanceof ConnectionFactory) {
					ConnectionFactory connectionFactory;

					connectionFactory = (ConnectionFactory) jndiObject;
					connection = connectionFactory.createConnection(ctx.getUser(), ctx.getPassword());
					result = true;
				}

			} catch (NamingException | JMSException e) {
				LogUtil.getInstance().error("could not create JMS connection using factory [" + factoryName + "]", e);
			}
		} else
			result = true;
		return result;
	}

	protected QueueSession getSession(boolean transaction) {
		QueueSession result = session;

		if (null == result) {
			try {
				result = session = (QueueSession) getConnection().createSession(transaction, Session.AUTO_ACKNOWLEDGE);
			} catch (JMSException e) {
				LogUtil.getInstance().error("unable to get session for [" + factoryName + "]", e);
			}
		}

		return result;
	}

	protected void closeSession(Session session) {
		if (null != session)
			try {
				session.close();
			} catch (JMSException e) {
				LogUtil.getInstance().error("unable to close session [" + getQueueName() + "]", e);
			} finally {
				session = null;
			}
	}

	public void close() {
		disconnect();
	}

	private boolean isConnected() {
		return null != connection;
	}

	public void disconnect() {
		try {
			ctx.disconnect();
			closeSession(session);
			if (null != connection)
				connection.close();
		} catch (NullPointerException | JMSException e) {
			LogUtil.getInstance().error("could not close JMS connection created with factory [" + factoryName + "]", e);
		} finally {
			connection = null;
		}
	}

	public static void main(String[] args) {
		WebLogicNamingContext ctx = new WebLogicNamingContext("localhost", 9021, "weblogic", "welcome1");

		String filename = "d:/tmp/large.dat";
		try (InputStream is = new FileInputStream(filename);
				OutputStream os = new FileOutputStream(filename + ".readback");
				JMSConnection conn = new JMSConnection(ctx, "jms.CFCustom")) {
			ctx.connect();
			JMSUtil ut = new JMSUtil();
			ut.print(ctx, DataUtil.PATH_SEPARATOR, System.out);
			conn.connect();
			// conn.setQueueName("CustomJMSServer/SMCustom!CustomInQueue");
			conn.setQueueName("jms.CustomInQueue");
			JMSPersist persist = new JMSPersist();
			// JMSCorrelation correlation = null;
			JMSCorrelation correlation = persist.save(conn, null, is, false);
			persist.read(conn, correlation, os, false);
			LogUtil.getInstance().info("whatever");
		} catch (IOException e) {
			LogUtil.getInstance().error("unable to open file [" + filename + "]", e);
		} finally {
			ctx.disconnect();
		}
	}

	public NamingContext getNamingContext() {
		return ctx;
	}

	public void setNamingContext(NamingContext ctx) {
		this.ctx = ctx;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
		this.queue = null;
	}

	public void setQueue(Destination destination) {
		this.queue = destination;
		if (this.queue instanceof Queue)
			try {
				this.queueName = ((Queue) destination).getQueueName();
			} catch (JMSException e) {
				LogUtil.getInstance().error("unable to set queue name", e);
			}
	}

	public Destination getQueue() {
		if (null == queue) {
			try {
				queue = (Queue) getNamingContext().getContext().lookup(getQueueName());
			} catch (NamingException e) {
				LogUtil.getInstance().error("unable to find queue [" + getQueueName() + "]", e);
			}
		}
		return queue;
	}

}
