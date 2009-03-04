package org.springframework.flex.messaging.security;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
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
public class EndpointInterceptor extends AbstractSecurityInterceptor implements MethodInterceptor{

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

	public Object invoke(MethodInvocation mi) throws Throwable {
		Message message = (Message) mi.getArguments()[0];
		if (isPassThroughCommand(message)) {
			return mi.proceed();
		} else {
			Object result = null;
			InterceptorStatusToken token = beforeInvocation(mi.getThis());
	
			try {
				result = mi.proceed();
			} finally {
				result = afterInvocation(token, result);
			}
	
			return result;
		}
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
