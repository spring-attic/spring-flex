package org.springframework.flex.security;

import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.messages.Message;

public class PerClientAuthenticationInterceptor implements MessageInterceptor {

	public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
		SecurityContextHolder.clearContext();
		return outputMessage;
	}

	public Message preProcess(MessageProcessingContext context, Message inputMessage) {
		if (FlexContext.getUserPrincipal() != null) {
			Assert.isInstanceOf(Authentication.class, FlexContext.getUserPrincipal(), "FlexContext.getUserPrincipal returned an unexpected type.  " +
					"Expected instance of "+Authentication.class.getName()+"but was "+FlexContext.getUserPrincipal().getName());
			SecurityContextHolder.getContext().setAuthentication((Authentication) FlexContext.getUserPrincipal());
		}
		return inputMessage;
	}

}
