package nl.ordina.tools.soa.osb.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import data.ConfigurationUtil;
import data.LogUtil;
import jee.jms.JMSConnection;
import jee.jms.JMSCorrelation;
import jee.jms.JMSPersist;
import jee.jndi.WebLogicNamingContext;

/**
 * <pre>
 * This class implements system [B], using the message id for correlation. 
 *  [     ]  --(1)-> (  send queue ) --(2)->  [     ]
 *  [  A  ]                                   [  B  ]
 *  [     ]  <-(4)-- (receive queue) <-(3)--  [     ]
 * </pre>
 */
public class OSBTestJMS extends OSBTestBase {
	private String server;
	private boolean running;

	public OSBTestJMS(String server) {
		this.server = server;
	}

	public void init() {
		readConfig();
	}

	public void start() {
		running = true;
		WebLogicNamingContext ctx = new WebLogicNamingContext(
				ConfigurationUtil.getInstance().getSetting(
						OSBTestConst.JMS_HOSTNAME.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()), "localhost"),
				ConfigurationUtil.getInstance().getIntegerSetting(
						OSBTestConst.JMS_PORT.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()), -1),
				ConfigurationUtil.getInstance().getSetting(
						OSBTestConst.WLS_JMS_USER.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()), "weblogic"),
				ConfigurationUtil.getInstance().getSetting(
						OSBTestConst.WLS_JMS_CREDENTIALS.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()),
						"welcome1"));
		JMSConnection connRecv = new JMSConnection(ctx,
				ConfigurationUtil.getInstance().getSetting(
						OSBTestConst.WLS_JMS_CONNECTION_FACTORY.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()),
						"jms.CFCustom"));
		JMSConnection connSend = new JMSConnection(ctx,
				ConfigurationUtil.getInstance().getSetting(
						OSBTestConst.WLS_JMS_CONNECTION_FACTORY.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()),
						"jms.CFCustom"));
		try {
			ctx.connect();
			connRecv.connect();
			connSend.connect();
			String queueNameRecv = ConfigurationUtil.getInstance().getSetting(
					OSBTestConst.WLS_JMS_RECEIVE_QUEUE.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()),
					"jms.CustomInQueue");
			connRecv.setQueueName(queueNameRecv);
			String queueNameSend = ConfigurationUtil.getInstance().getSetting(
					OSBTestConst.WLS_JMS_SEND_QUEUE.replace(OSBTestConst.SERVER_PLACEHOLDER, getServer()),
					"jms.CustomOutQueue");
			connSend.setQueueName(queueNameSend);
			while (running) {
				try (OutputStream os = new ByteArrayOutputStream()) {
					JMSPersist persist = new JMSPersist();
					LogUtil.getInstance().info("receive from queue [" + queueNameRecv + "]");
					JMSCorrelation correlation = persist.read(connRecv, null, os, false);
					if (null != correlation)
						LogUtil.getInstance().info("receive message with id [" + correlation.getMessageId() + "]");
					else
						LogUtil.getInstance().info("nothing received");

					String msg = os.toString();
					msg = msg + "resultaat";
					LogUtil.getInstance().info("send to queue [" + queueNameSend + "]");
					correlation.clearProperties();
					correlation.getProperties().put("CUST_String1", new Date().toString());
					correlation.useMessageIdForCorrelation(true);					
					correlation = persist.save(connSend, correlation, new ByteArrayInputStream(msg.getBytes()), false);
					LogUtil.getInstance().info("send output to queue done");
				} catch (IOException e) {
					LogUtil.getInstance().error("problem running service", e);
				}
			}
		} finally {
			ctx.disconnect();
			connRecv.disconnect();
			connSend.disconnect();
		}
	}

	public void stop() {
		running = false;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}
