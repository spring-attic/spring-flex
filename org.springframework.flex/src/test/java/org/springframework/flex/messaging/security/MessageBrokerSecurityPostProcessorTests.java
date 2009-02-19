package org.springframework.flex.messaging.security;

import static org.mockito.Mockito.*;

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

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;

public class MessageBrokerSecurityPostProcessorTests extends TestCase {

	MessageBroker broker;
	@Mock AbstractEndpoint endpoint1;
	@Mock AbstractEndpoint endpoint2;
	@Mock MethodInterceptor advice1;
	@Mock MethodInterceptor advice2;
	EndpointServiceMessagePointcutAdvisor advisor1;
	EndpointServiceMessagePointcutAdvisor advisor2;
	MessageBrokerSecurityPostProcessor processor;
	
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
	public void testPostProcessAfterInit() {
		
		List<EndpointSecurityAdvisor> advisors = new ArrayList<EndpointSecurityAdvisor>();
		advisors.add(advisor1);
		advisors.add(advisor2);
		processor = new MessageBrokerSecurityPostProcessor(advisors);
		
		MessageBroker processedBroker = (MessageBroker) processor.postProcessAfterInitialization(broker, "myMessageBroker");
		assertSame(broker, processedBroker);
		
		Collection endpoints = processedBroker.getEndpoints().values();
		
		Iterator i = endpoints.iterator();
		while(i.hasNext()) {
			Endpoint endpoint = (Endpoint) i.next();
			
			assertTrue(endpoint instanceof AbstractEndpoint);
			assertTrue(endpoint instanceof Advised);
			
			Advised advisedEndpoint = (Advised) endpoint;
			
			assertTrue(advisedEndpoint.getAdvisors().length == 2);
			assertTrue(advisedEndpoint.isFrozen());
			assertTrue(advisedEndpoint.isProxyTargetClass());
			assertTrue(advisedEndpoint.indexOf(advisor1) == 0);
			assertTrue(advisedEndpoint.indexOf(advisor2) == 1);
		}
	}
}
