package flex.spring.samples.integration;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import flex.messaging.messages.Message;

public class Counter {

	private volatile boolean running;

	private Log log = LogFactory.getLog(Counter.class);

	private final AtomicInteger count = new AtomicInteger();

	public Integer next() {
		return (this.running) ? count.getAndIncrement() : null;
	}

	public void handle(Message message) {
		String s = message.getBody().toString();
		if ("start".equalsIgnoreCase(s)) {
			this.running = true;
		}
		else if ("stop".equalsIgnoreCase(s)) {
			this.running = false;
		}
		else {
			try {
				count.set(Integer.parseInt(s));
			}
			catch (NumberFormatException e) {
				log.info("UNSUPPORTED FLEX MESSAGE RECEIVED: " + message);				
			}
		}
	}

}
