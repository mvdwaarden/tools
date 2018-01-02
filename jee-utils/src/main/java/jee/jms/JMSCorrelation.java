package jee.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import persist.PersistId;

public class JMSCorrelation implements PersistId<String> {
	private String messageId;
	private String correlationId;
	private Destination replyTo;
	private Map<String, String> properties;

	public String getMessageId() {
		return messageId;
	}

	public String getId() {
		return getMessageId();
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public void useMessageIdForCorrelation(boolean testEmpty) {
		if (!testEmpty || (null == getCorrelationId() || getCorrelationId().isEmpty()))
			setCorrelationId(getMessageId());
	}

	public Destination getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(Destination replyTo) {
		this.replyTo = replyTo;
	}

	public Map<String, String> getProperties() {
		if (null == properties) {
			properties = new HashMap<>();
		}

		return properties;
	}

	public void clearProperties() {
		if (null != properties)
			properties = null;
	}
}
