package org.springframework.flex.messaging.security;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationException;
import org.springframework.security.intercept.AbstractSecurityInterceptor;
import org.springframework.security.intercept.InterceptorStatusToken;
import org.springframework.security.intercept.ObjectDefinitionSource;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.messages.Message;
import flex.messaging.security.SecurityException;

public class EndpointInterceptor extends AbstractSecurityInterceptor {

	private EndpointDefinitionSource objectDefinitionSource;
	
	private MessageBroker broker;
	
	public EndpointInterceptor(MessageBroker broker) {
		Assert.notNull(broker, "A MessageBroker is required.");
		this.broker = broker;
	}

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

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Iterator i = broker.getEndpoints().keySet().iterator(); 
		while (i.hasNext()) {
			String key = (String) i.next();
			Endpoint endpoint = (Endpoint) broker.getEndpoints().get(key);
			ProxyFactory factory = new ProxyFactory();
			factory.setProxyTargetClass(true);
			factory.addAdvisor(new ServiceMessagePointcutAdvisor());
			factory.setTarget(endpoint);
			broker.getEndpoints().put(key, factory.getProxy());
		}		
	}

	private class ServiceMessageInterceptor implements MethodInterceptor {

		public Object invoke(MethodInvocation mi) throws Throwable {
			try{
				return doInvoke(mi);
			} catch(AuthenticationException ex) {
				SecurityException se = new SecurityException();
				se.setCode(SecurityException.CLIENT_AUTHENTICATION_CODE);
				se.setMessage(ex.getLocalizedMessage());
				se.setRootCause(ex);
				throw se;
			} catch (AccessDeniedException ex) {
				SecurityException se = new SecurityException();
				se.setCode(SecurityException.CLIENT_AUTHORIZATION_CODE);
				se.setMessage(ex.getLocalizedMessage());
				se.setRootCause(ex);
				throw se;
			}
		}

		private Object doInvoke(MethodInvocation mi) throws Throwable {
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
	
	private class ServiceMessagePointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {

		private static final String SERVICE_MESSAGE_METHOD_NAME = "serviceMessage";
		private final Class[] SERVICE_MESSAGE_ARGS = new Class[] {Message.class};
		
		public ServiceMessagePointcutAdvisor() {
			super(new ServiceMessageInterceptor());
		}

		public boolean matches(Method m, Class c) {
			return AbstractEndpoint.class.isAssignableFrom(c) &&
			m.equals(ReflectionUtils.findMethod(AbstractEndpoint.class, SERVICE_MESSAGE_METHOD_NAME, SERVICE_MESSAGE_ARGS));
		}
	}
}
