package org.dwbzen.util.messaging;

import java.util.LinkedList;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

public class MessageProducerImpl implements javax.jms.MessageProducer {

	MessageProducer messageProducer = null;
	Destination destination = null;
	private int deliveryMode = 0;
	private boolean disableMessageID = false;
	private boolean disableMessageTimestamp = false;
	private int priority = 0;
	private long timeToLive = 0;
	private LinkedList<Message> queue;
	
	public MessageProducerImpl() {
		queue = new LinkedList<Message>();
	}
	
	public MessageProducerImpl(Destination dest) {
		this(null, dest);
	}

	public MessageProducerImpl(MessageProducer producer, Destination dest) {
		destination = dest;
		messageProducer = producer;		// could be null
	}
	
	public MessageProducer getMessageProducer() {
		return messageProducer;
	}

	public void setMessageProducer(MessageProducer messageProducer) {
		this.messageProducer = messageProducer;
	}

	@Override
	public void close() throws JMSException {
		if(messageProducer != null) {
			messageProducer.close();
		}
	}

	@Override
	public int getDeliveryMode() throws JMSException {
		return messageProducer!=null ? messageProducer.getDeliveryMode() : deliveryMode;
	}

	@Override
	public Destination getDestination() throws JMSException {
		return destination;
	}

	@Override
	public boolean getDisableMessageID() throws JMSException {
		return messageProducer!=null ? messageProducer.getDisableMessageID() : disableMessageID;
	}

	@Override
	public boolean getDisableMessageTimestamp() throws JMSException {
		return  messageProducer!=null ? messageProducer.getDisableMessageTimestamp() : disableMessageTimestamp;
	}

	@Override
	public int getPriority() throws JMSException {
		return  messageProducer!=null ? messageProducer.getPriority() : priority;
	}

	@Override
	public long getTimeToLive() throws JMSException {
		return messageProducer!=null ? messageProducer.getTimeToLive() : timeToLive;
	}

	@Override
	public void send(Message msg) throws JMSException {
		if(messageProducer != null) {
			messageProducer.send(msg);
		}
		else {
			queue.add(msg);
		}
	}

	@Override
	public void send(Destination dest, Message msg) throws JMSException {
		if(messageProducer != null) {
			messageProducer.send(dest, msg);
		}
	}

	@Override
	public void send(Message msg, int arg1, int arg2, long arg3) throws JMSException {
		if(messageProducer != null) {
			messageProducer.send(msg, arg1, arg2, arg3);
		}
	}

	@Override
	public void send(Destination dest, Message arg1, int arg2, int arg3, long arg4) throws JMSException {
		if(messageProducer != null) {
			messageProducer.send(dest, arg1, arg2, arg3, arg4);
		}		
	}

	@Override
	public void setDeliveryMode(int arg0) throws JMSException {
		if(messageProducer != null) {
			messageProducer.setDeliveryMode(arg0);
		}
		else {
			deliveryMode = arg0;
		}
	}

	@Override
	public void setDisableMessageID(boolean arg0) throws JMSException {
		if(messageProducer != null) {
			messageProducer.setDisableMessageID(arg0);
		}
		else {
			disableMessageID = arg0;
		}
		
	}

	@Override
	public void setDisableMessageTimestamp(boolean arg0) throws JMSException {
		if(messageProducer != null) {
			messageProducer.setDisableMessageTimestamp(arg0);
		}
		else {
			disableMessageTimestamp = arg0;
		}		
	}

	@Override
	public void setPriority(int arg0) throws JMSException {
		if(messageProducer != null) {
			messageProducer.setPriority(arg0);
		}
		else {
			priority = arg0;
		}		
	}

	@Override
	public void setTimeToLive(long arg0) throws JMSException {
		if(messageProducer != null) {
			messageProducer.setTimeToLive(arg0);
		}
		else {
			timeToLive = arg0;
		}		
	}

}
