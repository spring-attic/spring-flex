package org.springframework.flex.config.xml;

import java.util.Arrays;
import java.util.Iterator;

import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.BeanIds;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class RemotingDestinationBeanDefinitionParserTests extends AbstractFlexConfigurationTests {
	
	private MessageBroker broker;
	
	public void testExportBeanWithDefaults() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		RemotingService rs = (RemotingService) broker.getService("remoting-service");
		assertNotNull("Could not find the remoting service", rs);
		RemotingDestination rd = (RemotingDestination) rs.getDestination("remoteBean1");
		assertNotNull("Destination not found", rd);
	}
	
	@SuppressWarnings("unchecked")
	public void testExportBeanWithCustomSettings() {
		broker = (MessageBroker) getApplicationContext().getBean("remoteServiceBroker", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		RemotingService rs = (RemotingService) broker.getService("remoting-service");
		assertNotNull("Could not find the remoting service", rs);
		RemotingDestination rd = (RemotingDestination) rs.getDestination("exportedRemoteBean1");
		assertNotNull("Destination not found", rd);
		String[] channels = new String[] {"my-amf", "my-secure-amf"};
		assertEquals("Channels not set",Arrays.asList(channels), rd.getChannels());
		
		assertTrue("Custom adapter not set", rd.getAdapter() instanceof TestAdapter);
		
		String[] includeNames = new String[]{ "foo", "bar" };
		String[] excludeNames = new String[]{ "zoo", "baz" };
		
		assertTrue("No included methods found",((JavaAdapter)rd.getAdapter()).getIncludeMethodIterator().hasNext());
		Iterator includes = ((JavaAdapter)rd.getAdapter()).getIncludeMethodIterator();
		while(includes.hasNext()) {
			RemotingMethod include = (RemotingMethod) includes.next();
			assertTrue(Arrays.asList(includeNames).contains(include.getName()));
			assertFalse(Arrays.asList(excludeNames).contains(include.getName()));
		}
		
		assertTrue("No excluded methods found",((JavaAdapter)rd.getAdapter()).getExcludeMethodIterator().hasNext());
		Iterator excludes = ((JavaAdapter)rd.getAdapter()).getExcludeMethodIterator();
		while(includes.hasNext()) {
			RemotingMethod exclude = (RemotingMethod) excludes.next();
			assertTrue(Arrays.asList(excludeNames).contains(exclude.getName()));
			assertFalse(Arrays.asList(includeNames).contains(exclude.getName()));
		}
	}
	
	public void testInvalidConfig() {
		try {
			new ClassPathXmlApplicationContext("org/springframework/flex/config/invalid-remote-service.xml");
			fail("Invalid message-broker config was not caught");
		} catch (BeanDefinitionParsingException ex) {
			//Expected
		}
	}
	
	public static final class Bean1 { 
		public String foo() { return "foo"; }
		public String bar() { return "bar"; }
		public String zoo() { return "zoo"; }
		public String baz() { return "baz"; }
	}
	
	public static final class TestAdapter extends JavaAdapter {}
}
