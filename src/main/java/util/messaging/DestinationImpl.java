package util.messaging;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

public class DestinationImpl implements Queue {

	Destination destination = null;
	private String name;
	
	public DestinationImpl(String name) {
		this.name = name;
	}
	
	public DestinationImpl(Destination dest) {
		destination = dest;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getQueueName() throws JMSException {
		return name;
	}
	
}
