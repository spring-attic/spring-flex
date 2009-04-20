package org.springframework.flex.core;

import flex.messaging.messages.Message;

public interface MessageInterceptor {

	public Message preProcess(Message inputMessage);
	
	public Message postProcess(Message inputMessage, Message outputMessage);
}
