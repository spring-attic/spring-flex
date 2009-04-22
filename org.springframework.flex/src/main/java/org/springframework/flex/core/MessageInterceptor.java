package org.springframework.flex.core;

import flex.messaging.messages.Message;

public interface MessageInterceptor {

	public Message preProcess(MessageProcessingContext context, Message inputMessage);
	
	public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage);
}
