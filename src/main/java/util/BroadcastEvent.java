package util;

public class BroadcastEvent {
	private Number numberPayload = null;
	private Object objectPayload = null;

	public static enum EVENT_TYPE {Iteration, IterationComplete};
	
	private EVENT_TYPE eventType;
	
	public BroadcastEvent(EVENT_TYPE etype) {
		eventType = etype;
	}
	
	/**
	 * @return the numberPayload
	 */
	public Number getNumberPayload() {
		return numberPayload;
	}
	/**
	 * @param numberPayload the numberPayload to set
	 */
	public void setNumberPayload(Number numberPayload) {
		this.numberPayload = numberPayload;
	}
	/**
	 * @return the objectPayload
	 */
	public Object getObjectPayload() {
		return objectPayload;
	}
	/**
	 * @param objectPayload the objectPayload to set
	 */
	public void setObjectPayload(Object objectPayload) {
		this.objectPayload = objectPayload;
	}
	public EVENT_TYPE getEventType() {
		return eventType;
	}
	public void setEventType(EVENT_TYPE eventType) {
		this.eventType = eventType;
	}
	
}
