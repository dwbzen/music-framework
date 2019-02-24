package music;

import java.util.Map;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.JsonObject;

public class ScorePartMessageListener implements Runnable, MessageListener, ExceptionListener {
	protected static final Logger log = LogManager.getLogger(ScorePartMessageListener.class);

    private ScorePart scorePart = null;
    private int messageCount = 0;
    private MessageConsumer consumer;
    private boolean more = true;
    public static String SHUTDOWN_COMMAND = "SHUTDOWN";	// needs to match the final command message
    
    public ScorePartMessageListener(ScorePart sp, MessageConsumer consumer) {
    	this.scorePart = sp;
    	this.consumer = consumer;
    }

	public void run() {
		
		try {
			while(more) {	// messages in the instrument queue
				Message message = consumer.receive();
				log.debug("Received a message " +  message.toString());
				if(message instanceof TextMessage)  {
					String messageText = getTextMessage(message);
					processMessageText(messageText);
				}
				else if(message instanceof  ActiveMQMapMessage) {
					ActiveMQMapMessage mapmsg = (ActiveMQMapMessage)message;
					try {
						Map<String, Object> contentMap = mapmsg.getContentMap();
						processMapMessage(contentMap);
					} catch (JMSException e) {
						log.error("JMSException: " + e.toString());
						log.error("message: ", message);
					}
				}
			}
		} catch(Exception e) {
			log.error("Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private String getTextMessage(Message message) {
		String messageText = null;
		try {
			messageText = ((TextMessage)message).getText();
		} catch (JMSException e1) {
			log.error("processTextMessage exception: " + e1.toString());
			e1.printStackTrace();
			return null;
		}
		return messageText;
	}
	
	public void processMessageText(String messageText) {
		JsonObject jsonObj = JsonObject.analyzeMessage(messageText);
		if(jsonObj == null) {
			log.error("processMessageText JSONObject is null: " + messageText);
			return;
		}
        if (jsonObj.getType().equals("message")) {
        	if(checkText(messageText, SHUTDOWN_COMMAND)) {
	            try {
	            	log.info("Shutting down " + getScorePart().getPartName() + " after " + this.messageCount + " messages");
	            } catch (Exception e) {
	                e.printStackTrace(System.out);
	            }
	            more = false;
        	}
        	else if(checkText(messageText, "START")) {
        		// Should not see these, but can be safely ignored if present
        	}
        } 
        else {
            if (++messageCount % 100 == 0) {
                log.trace("Received " + messageCount + " messages.");
            }
        }
        /*
         * Now do something with this message
         */
        scorePart.processMessage(jsonObj);

	}
	
	private void processMapMessage(Map<String, Object> contentMap) {

	}

	/**
	 * Checks if Message is a TextMessage and if so,
	 * if the text matches the String provided - ignores case by the way
	 * @param message Object to check
	 * @param s String to check against
	 * @return true if equalsIgnoreCase or s is in the source string
	 */
    protected static boolean checkText(Object obj, String s) {
    	String message = (obj != null) ? obj.toString() : "";
        return message.equalsIgnoreCase(s) || message.indexOf(s) > 0;
    }

	public ScorePart getScorePart() {
		return scorePart;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public boolean isMore() {
		return more;
	}

	public void setMore(boolean more) {
		this.more = more;
	}

	@Override
	public void onMessage(Message message) {
		// for real time processing
		log.trace("onMessage: " + message.toString());
		if(message instanceof TextMessage)  {
			processMessageText(getTextMessage(message));
		}
	}

	@Override
	public void onException(JMSException exception) {
		// for real time processing
		log.error("JMSException: " + exception.toString());
		more = false;
	}

}
