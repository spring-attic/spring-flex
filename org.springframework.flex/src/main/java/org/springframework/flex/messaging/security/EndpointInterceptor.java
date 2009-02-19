package org.springframework.flex.messaging.security;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.intercept.AbstractSecurityInterceptor;
import org.springframework.security.intercept.InterceptorStatusToken;
import org.springframework.security.intercept.ObjectDefinitionSource;

import flex.messaging.endpoints.AbstractEndpoint;

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
