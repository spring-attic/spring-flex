package org.springframework.flex.security;

import org.springframework.flex.core.MessageInterceptionContext;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.security.intercept.AbstractSecurityInterceptor;
import org.springframework.security.intercept.InterceptorStatusToken;
import org.springframework.security.intercept.ObjectDefinitionSource;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * Security interceptor that secures messages being passed to BlazeDS endpoints based on the security attributes
 * configured for the endpoint being invoked.
 *  
 * @author Jeremy Grelle
 */
@SuppressWarnings("unchecked")
public class EndpointInterceptor extends AbstractSecurityInterceptor implements MessageInterceptor{

	private static final String STATUS_TOKEN = "_enpointInterceptorStatusToken";
	
	private EndpointDefinitionSource objectDefinitionSource;

	public Class getSecureObjectClass() {
		return AbstractEndpoint.class;
	}

	public ObjectDefinitionSource obtainObjectDefinitionSource() {
		return objectDefinitionSource;
	}

	public EndpointDefinitionSource getObjectDefinitionSource() {
		return objectDefinitionSource;
	}

	public void setObjectDefinitionSource(EndpointDefinitionSource newSource) {
		objectDefinitionSource = newSource;
	}
	
	public Message postProcess(MessageInterceptionContext context, Message inputMessage, Message outputMessage) {
		if (context.getAttributes().containsKey(STATUS_TOKEN)) {
			InterceptorStatusToken token = (InterceptorStatusToken) context.getAttributes().get(STATUS_TOKEN);
			return (Message) afterInvocation(token, outputMessage);
		} else {
			return outputMessage;
		}
	}

	public Message preProcess(MessageInterceptionContext context, Message inputMessage) {
		if (!isPassThroughCommand(inputMessage)) {
			InterceptorStatusToken token = beforeInvocation(context.getMessageTarget());
			context.getAttributes().put(STATUS_TOKEN, token);
		}
		return inputMessage;
	}

	private boolean isPassThroughCommand(Message message) {
		if (message instanceof CommandMessage) {
			CommandMessage command = (CommandMessage) message;
			return (command.getOperation() == CommandMessage.CLIENT_PING_OPERATION ||
					command.getOperation() == CommandMessage.LOGIN_OPERATION);
		}
		return false;
	}
}
