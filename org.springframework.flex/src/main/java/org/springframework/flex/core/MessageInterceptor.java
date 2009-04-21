package org.springframework.flex.core;

import flex.messaging.messages.Message;

public interface MessageInterceptor {

	public Message preProcess(MessageInterceptionContext context, Message inputMessage);
	
	public Message postProcess(MessageInterceptionContext context, Message inputMessage, Message outputMessage);
}
