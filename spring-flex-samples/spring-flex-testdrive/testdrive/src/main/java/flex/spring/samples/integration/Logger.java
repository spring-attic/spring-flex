package flex.spring.samples.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Logger {

	Log log = LogFactory.getLog(Logger.class);
	
	public void display(Object o) {
		log.info("INCOMING FLEX MESSAGE RECEIVED: " + o);
	}

}
