package org.springframework.flex.integration.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.flex.messaging.MessageTemplate;

public class PingService implements InitializingBean, Ping {

	private MessageTemplate template;
	
	private TaskExecutor taskExecutor;
	
	/* (non-Javadoc)
	 * @see org.springframework.flex.integration.service.Ping#ping()
	 */
	public String ping() {
		return "pong";
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.flex.integration.service.Ping#foo()
	 */
	public String foo() {
		return "bar";
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.flex.integration.service.Ping#fireEvent()
	 */
	public void fireEvent() {
		taskExecutor.execute(new EventPublisher());
	}
	
	private final class EventPublisher implements Runnable {

		public void run() {
			template.send("event-bus", "fired");
		}
	}

	public void setTemplate(MessageTemplate template) {
		this.template = template;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void afterPropertiesSet() throws Exception {
		System.out.println("_____________________INITIALIZING PING SERVICE______________________");
	}
	
}
