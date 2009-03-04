package org.springframework.flex.messaging.security;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.util.ReflectionUtils;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;
import flex.messaging.services.Service;

/**
 * Static method-matching pointcut advisor that applies security advice to invocations of {@link AbstractEndpoint#serviceMessage(Message)}.
 * 
 * <p>
 * This is the critical point where incoming AMF messages have been deserialized and are ready to be routed to a BlazeDS {@link Service}.
 * Iterception at this point in the deserialization/serialization process is needed to be able to easily send proper AMF error messages back to
 * the client.
 * 
 * @author Jeremy Grelle
 */
@SuppressWarnings({ "unchecked", "serial" })
public class EndpointServiceMessagePointcutAdvisor extends
		StaticMethodMatcherPointcutAdvisor implements EndpointSecurityAdvisor {

	private static final String SERVICE_MESSAGE_METHOD_NAME = "serviceMessage";
	
	private final Class[] SERVICE_MESSAGE_ARGS = new Class[] {Message.class};
	
	public EndpointServiceMessagePointcutAdvisor(Advice advice) {
		super(advice);
	}

	public boolean matches(Method m, Class c) {
		return AbstractEndpoint.class.isAssignableFrom(c) &&
		m.equals(ReflectionUtils.findMethod(AbstractEndpoint.class, SERVICE_MESSAGE_METHOD_NAME, SERVICE_MESSAGE_ARGS));
	}
}
