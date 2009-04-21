package org.springframework.flex.config;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.Advised;
import org.springframework.flex.config.MessageBrokerEndpointConfigProcessor;
import org.springframework.flex.core.EndpointAdvisor;
import org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor;
import org.springframework.util.ReflectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.BaseHTTPEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.endpoints.amf.AMFFilter;

public class MessageBrokerEndpointConfigProcessorTests extends TestCase {

	MessageBroker broker;
	@Mock
	AbstractEndpoint endpoint1;
	@Mock
	AbstractEndpoint endpoint2;
	@Mock
	MethodInterceptor advice1;
	@Mock
	MethodInterceptor advice2;
	EndpointServiceMessagePointcutAdvisor advisor1;
	EndpointServiceMessagePointcutAdvisor advisor2;
	MessageBrokerEndpointConfigProcessor processor;

	@SuppressWarnings("unchecked")
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		advisor1 = new EndpointServiceMessagePointcutAdvisor(advice1);
		advisor2 = new EndpointServiceMessagePointcutAdvisor(advice2);

		broker = new MessageBroker();
		Map endpoints = new HashMap();
		endpoints.put("foo", endpoint1);

		when(endpoint1.getId()).thenReturn("bar");
		when(endpoint1.getUrl()).thenReturn("http://foo.com/bar");
		broker.addEndpoint(endpoint1);

		when(endpoint2.getId()).thenReturn("baz");
		when(endpoint2.getUrl()).thenReturn("http://foo.com/baz");
		broker.addEndpoint(endpoint2);
	}

	@SuppressWarnings("unchecked")
	public void testPostProcessAfterInit() throws Exception {

		List<EndpointAdvisor> advisors = new ArrayList<EndpointAdvisor>();
		advisors.add(advisor1);
		advisors.add(advisor2);
		processor = new MessageBrokerEndpointConfigProcessor(advisors);

		MessageBroker processedBroker = (MessageBroker) processor
				.processAfterStartup(broker);
		assertSame(broker, processedBroker);

		Collection endpoints = processedBroker.getEndpoints().values();

		Iterator i = endpoints.iterator();
		while (i.hasNext()) {
			Endpoint endpoint = (Endpoint) i.next();

			assertTrue(endpoint instanceof AbstractEndpoint);
			assertTrue(endpoint instanceof Advised);
			
			Advised advisedEndpoint = (Advised) endpoint;

			assertTrue(advisedEndpoint.getAdvisors().length == 2);
			assertTrue(advisedEndpoint.isFrozen());
			assertTrue(advisedEndpoint.isProxyTargetClass());
			assertTrue(advisedEndpoint.indexOf(advisor1) == 0);
			assertTrue(advisedEndpoint.indexOf(advisor2) == 1);
			
			Object targetEndpoint = advisedEndpoint.getTargetSource().getTarget();
			if (targetEndpoint instanceof BaseHTTPEndpoint) {
				Field filterChainField = ReflectionUtils.findField(targetEndpoint.getClass(), "filterChain");
				assertNotNull("Endpoint should have a filterChain field",filterChainField);
				AMFFilter filter = (AMFFilter) ReflectionUtils.getField(filterChainField, targetEndpoint);
				assertNotNull("Endpoint should have a populated filterChain field");
				while (filter != null) {
					Field endpointField = ReflectionUtils.findField(filter.getClass(), "endpoint");
					if (endpointField != null) {
						Object endpointValue = ReflectionUtils.getField(endpointField, filter);
						assertSame("AMFFilter's endpoint field should be the proxy instance", advisedEndpoint, endpointValue);
					}
					filter = filter.getNext();
				}
			}
		}
	}

}
