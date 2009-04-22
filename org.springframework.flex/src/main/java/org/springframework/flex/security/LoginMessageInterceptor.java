package org.springframework.flex.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class LoginMessageInterceptor implements MessageInterceptor {

	private static final String SUCCESS_MSG = "success";
	
	public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
		if (inputMessage instanceof CommandMessage && ((CommandMessage)inputMessage).getOperation() == CommandMessage.LOGIN_OPERATION) {
			if (SUCCESS_MSG.equals(outputMessage.getBody())) {
				outputMessage.setBody(getAuthenticationResult());
			}
		}
		return outputMessage;
	}

	private Map<String, Object> getAuthenticationResult() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> authenticationResult = new HashMap<String, Object>();
		authenticationResult.put("name", authentication.getName());
		String[] authorities = new String[authentication.getAuthorities().length];
		for (int i=0; i<authorities.length; i++) {
			authorities[i] = authentication.getAuthorities()[i].getAuthority();
		}
		authenticationResult.put("authorities", authorities);
		return authenticationResult;
	}

	public Message preProcess(MessageProcessingContext context, Message inputMessage) {
		return inputMessage;
	}

}
