package org.springframework.flex.security;

import org.springframework.flex.core.MessageInterceptor;
import org.springframework.security.context.SecurityContextHolder;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class LoginMessageInterceptor implements MessageInterceptor {

	private static final String SUCCESS_MSG = "success";
	
	public Message postProcess(Message inputMessage, Message outputMessage) {
		if (inputMessage instanceof CommandMessage && ((CommandMessage)inputMessage).getOperation() == CommandMessage.LOGIN_OPERATION) {
			if (SUCCESS_MSG.equals(outputMessage.getBody())) {
				outputMessage.setBody(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
			}
		}
		return outputMessage;
	}

	public Message preProcess(Message inputMessage) {
		return inputMessage;
	}

}
