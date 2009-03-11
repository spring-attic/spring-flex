package org.springframework.flex.messaging.config;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.flex.messaging.security.EndpointSecurityAdvisor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.amf.AMFFilter;

/**
 * Processor that applies advice to configured BlazeDS endpoints by wrapping them in Spring AOP proxies.
 *  
 * <p>
 * This processor will be automatically configured through the <code>message-broker</code> xml configuration namespace tag.
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerEndpointConfigProcessor implements
		MessageBrokerConfigProcessor, BeanClassLoaderAware {

	private EndpointSecurityAdvisor[] advisors;
	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	public MessageBrokerEndpointConfigProcessor(
			List<EndpointSecurityAdvisor> advisors) {
		Assert
				.notEmpty(advisors,
						"A non-empty list of EndpointServiceMessagePointcutAdvisors is required");
		this.advisors = advisors.toArray(new EndpointSecurityAdvisor[advisors
				.size()]);
	}

	@SuppressWarnings("unchecked")
	public MessageBroker processAfterStartup(MessageBroker broker) {
		Iterator i = broker.getEndpoints().keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			Object endpoint = broker.getEndpoints().get(key);
			ProxyFactory factory = new ProxyFactory();
			factory.setProxyTargetClass(true);
			factory.addAllAdvisors(advisors);
			factory.setTarget(endpoint);
			factory.setFrozen(true);
			Object proxy = factory.getProxy(proxyClassLoader);
			fixFilterChain(endpoint, proxy);
			broker.getEndpoints().put(key, proxy);
		}
		return broker;
	}

	private void fixFilterChain(Object endpoint, Object proxy) {
		// This is a nasty workaround, required because the advised Endpoint
		// passes a reference to itself to some of the filters in the chain.
		// It would be nice if the endpoints actually exposed their filter chain for
		// easier modification.
		Field filterChainField = ReflectionUtils.findField(endpoint.getClass(),
				"filterChain");
		if (filterChainField != null) {
			Assert.isAssignable(AMFFilter.class, filterChainField.getType(),
					"filterChain field is expected to be of type AMFFilter");
			ReflectionUtils.makeAccessible(filterChainField);
			AMFFilter filter = (AMFFilter) ReflectionUtils.getField(
					filterChainField, endpoint);
			while (filter != null) {
				Field endpointField = ReflectionUtils.findField(filter
						.getClass(), "endpoint");
				if (endpointField != null) {
					ReflectionUtils.makeAccessible(endpointField);
					ReflectionUtils.setField(endpointField, filter, proxy);
				}
				filter = filter.getNext();
			}

		}
	}

	public MessageBroker processBeforeStartup(MessageBroker broker) {
		return broker;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		proxyClassLoader = classLoader;
	}
}
