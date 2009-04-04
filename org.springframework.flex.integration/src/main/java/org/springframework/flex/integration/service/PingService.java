package org.springframework.flex.integration.service;

import org.springframework.core.task.TaskExecutor;
import org.springframework.flex.messaging.MessageTemplate;

public class PingService {

	private MessageTemplate template;
	
	private TaskExecutor taskExecutor;
	
	public String ping() {
		return "pong";
	}
	
	public String foo() {
		return "bar";
	}
	
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
	
}
