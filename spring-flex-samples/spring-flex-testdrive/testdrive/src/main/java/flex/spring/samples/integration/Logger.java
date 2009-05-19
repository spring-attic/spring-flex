package flex.spring.samples.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import flex.messaging.messages.Message;

public class Logger {

	Log log = LogFactory.getLog(Logger.class);
	
	public void display(Object o) {
		Message message = (Message) o;
		log.info("INCOMING FLEX MESSAGE RECEIVED: " + message.getBody());
	}

}
