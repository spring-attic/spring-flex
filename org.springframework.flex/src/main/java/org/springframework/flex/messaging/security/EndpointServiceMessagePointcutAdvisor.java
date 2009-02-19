package org.springframework.flex.messaging.security;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.util.ReflectionUtils;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;

@SuppressWarnings("unchecked")
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
