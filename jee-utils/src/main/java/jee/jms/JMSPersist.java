package jee.jms;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import data.DataUtil;
import data.LogUtil;
import persist.Persist;
import persist.PersistId;

/**
 * <pre>
 *  [     ]  --(1)-> (  send queue ) --(2)->  [     ]
 *  [  A  ]                                   [  B  ]
 *  [     ]  <-(4)-- (receive queue) <-(3)--  [     ]
 * 
 * Interaction based on message id introduced at (1) to be used as the correlation id
 * 
 * System A => Use standard (overridden Persist) save/read methods:
 * (1) save() returns PersistId: The returned PersistId contains the message id, which is used for correlation.
 * (4) read(PersistId) : Use useMessageIdForCorrelation(true) on PersistId from (1) to 
 *     use the message id as the correlation id. 
 * 
 * System B => Use extra save/read method behaviour:
 * (2) read() returns PersistId : PersistId contains the message id, which must be used for correlation.
 * (3) save(PersistId) : Use useMessageIdForCorrelation(true) on PersistId from (2) to 
 *     use the message id as the correlation id.
 *
 * Alternative when system A uses an explicit correlation ID
 * 
 * System A => Use extra save method behaviour to explicitly define the correlation id:
 * (1) save(PersistId): The used PersistId contains a correlation id (used in JMS).
 * (4) read(PersistId) : The same PersistId object used at 1
 * 
 * </pre>
 * 
 * @author mwa17610
 *
 */
public class JMSPersist implements Persist<String, String> {
	private static final String JMS_PROPERTY_CORRELATION_ID = "JMSCorrelationID";
	boolean chunked = false;
	boolean streaming = false;

	@Override
	public JMSCorrelation save(persist.Connection conn, String str, boolean transaction) {
		return save((JMSConnection) conn, null, new ByteArrayInputStream(str.getBytes()), transaction);
	}

	@Override
	public String read(persist.Connection conn, Class<String> cls, PersistId<String> correlation, boolean transaction) {
		String result = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		read((JMSConnection) conn, (JMSCorrelation) correlation, bos, transaction);

		result = bos.toString();

		return result;
	}

	public JMSCorrelation save(JMSConnection conn, JMSCorrelation correlation, InputStream is,
			boolean createTransaction) {
		JMSCorrelation result = null;
		int sendbytes = 0;
		Session session = null;

		try {
			Message msg = null;
			session = conn.getConnection().createSession(createTransaction, Session.AUTO_ACKNOWLEDGE);
			if (session instanceof QueueSession) {
				MessageProducer producer = session.createProducer(conn.getQueue());

				if (streaming) {
					StreamMessage smsg = session.createStreamMessage();
					msg = smsg;
					if (chunked) {
						byte[] buf = new byte[100000];
						int readbytes = is.read(buf);
						while (readbytes > 0) {
							smsg.writeBytes(buf, 0, readbytes);
							++readbytes;
							readbytes = is.read(buf);
						}
					} else {
						smsg.writeString(DataUtil.getInstance().writeToStringBuilder(is).toString());
					}
				} else {
					TextMessage tmsg = session.createTextMessage();
					msg = tmsg;
					StringBuilder tmp = DataUtil.getInstance().writeToStringBuilder(is);
					byte[] buf = new byte[100000];
					int readbytes = is.read(buf);
					while (readbytes > 0) {
						tmp.append(new String(buf, 0, readbytes));
						++readbytes;
						readbytes = is.read(buf);
					}
					tmsg.setText(tmp.toString());
				}

				if (null != correlation) {
					copyProperties(correlation.getProperties(), msg);
					if (null != correlation.getCorrelationId() && !correlation.getCorrelationId().isEmpty())
						msg.setJMSCorrelationID(correlation.getCorrelationId());
				}

				producer.send(msg);
				producer.close();
				result = createCorrelation(msg);
			}
		} catch (JMSException | IOException e) {
			LogUtil.getInstance()
					.error("unable to save message on [" + conn.getQueueName() + "] at byte [" + sendbytes + "]", e);
		}

		return result;
	}

	private JMSCorrelation createCorrelation(Message msg) throws JMSException {
		JMSCorrelation result = new JMSCorrelation();

		result.setCorrelationId(msg.getJMSCorrelationID());
		result.setMessageId(msg.getJMSMessageID());
		result.setReplyTo(msg.getJMSReplyTo());
		copyProperties(msg, result.getProperties());
		return result;
	}

	public JMSCorrelation read(JMSConnection conn, JMSCorrelation correlation, OutputStream os, boolean transaction) {
		class Result {
			JMSCorrelation result;
		}
		;
		final Result result = new Result();
		boolean synchronous = true;
		QueueSession session = null;
		try {
			session = conn.getSession(transaction);
			if (session instanceof QueueSession) {
				if (conn.getConnection() instanceof QueueConnection) {
					Connection qconn = conn.getConnection();
					Destination queue = conn.getQueue();
					MessageConsumer consumer;
					if (null != correlation) {
						consumer = session.createConsumer(queue,
								JMS_PROPERTY_CORRELATION_ID + "='ID:" + correlation.getCorrelationId() + "'");
					} else {
						consumer = session.createConsumer(queue, null);
					}

					if (synchronous) {
						qconn.start();
						Message msg = consumer.receive();
						copyMessage(msg, os);
						qconn.stop();
						result.result = createCorrelation(msg);
					} else {
						class Done {
							public boolean done;
						}
						;
						Done done = new Done();
						done.done = false;
						MessageListener lsnr = new MessageListener() {
							@Override
							public void onMessage(Message msg) {
								if (msg instanceof TextMessage)
									copyMessage(msg, os);
								done.done = true;
								try {
									result.result = createCorrelation(msg);
									qconn.stop();
								} catch (JMSException e) {
									LogUtil.getInstance().error("could not stop receiving", e);
								}
							}
						};
						consumer.setMessageListener(lsnr);
						qconn.start();
						while (!done.done) {
							Thread.yield();
							try {
								Thread.sleep(100);
								qconn.stop();
								done.done = true;
							} catch (InterruptedException e) {
								LogUtil.getInstance().warning("quit sleep", e);
							}
						}
					}
				}
			}
		} catch (JMSException e) {
			LogUtil.getInstance().error("unable to read message on queue [" + conn.getQueueName() + "]", e);
		}

		return result.result;
	}

	@SuppressWarnings("unchecked")
	private boolean copyProperties(Message msg, Map<String, String> properties) {
		boolean result = true;
		Enumeration<String> en;
		try {
			en = (Enumeration<String>) msg.getPropertyNames();
			while (en.hasMoreElements()) {
				String name = en.nextElement();
				properties.put(name, msg.getObjectProperty(name).toString());
			}
		} catch (JMSException e) {
			LogUtil.getInstance().error("problem getting JMS properties", e);
			result = false;
		}

		return result;
	}

	private boolean copyProperties(Map<String, String> properties, Message msg) {
		boolean result = true;

		for (Entry<String, String> entry : properties.entrySet()) {
			try {
				msg.setStringProperty(entry.getKey(), entry.getValue());
			} catch (JMSException e) {
				LogUtil.getInstance().error("problem getting JMS properties", e);
				result = false;
				break;
			}
		}

		return result;
	}

	private boolean copyMessage(Message msg, OutputStream os) {
		boolean result = false;
		int totalreadbytes = 0;

		try (OutputStream bos = new BufferedOutputStream(os)) {
			if (msg instanceof TextMessage) {
				String content = ((TextMessage) msg).getText();
				if (null != content)
					bos.write(content.getBytes());
				bos.flush();
				result = true;
			} else if (msg instanceof StreamMessage) {
				if (chunked) {
					StreamMessage smsg = (StreamMessage) msg;
					byte[] buf = new byte[100000];
					int readbytes = smsg.readBytes(buf);
					while (readbytes > 0 || readbytes == -1) {
						if (readbytes > 0) {
							bos.write(buf, 0, readbytes);
							totalreadbytes += readbytes;
						}
						try {
							readbytes = smsg.readBytes(buf);
						} catch (MessageEOFException e) {
							LogUtil.getInstance().ignore("expected eof exception", e);
							readbytes = 0;
						}
					}
				} else {
					StreamMessage smsg = (StreamMessage) msg;
					String tmp = smsg.readString();
					bos.write(tmp.getBytes());
				}
				bos.flush();
				result = true;
			}
		} catch (IOException | JMSException e) {
			LogUtil.getInstance().error("unable to write text message, read [" + totalreadbytes + "]", e);
		}
		return result;
	}
}
