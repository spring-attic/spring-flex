package org.springframework.flex.messaging.config;

import java.util.Arrays;
import java.util.Iterator;

import org.springframework.flex.messaging.remoting.FlexExclude;
import org.springframework.flex.messaging.remoting.FlexInclude;
import org.springframework.flex.messaging.remoting.FlexService;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;


public class FlexRemotingAnnotationPostProcessorTests extends
		AbstractFlexConfigurationTests {

	private MessageBroker broker;
	
	public void testExportAnnotatedXmlConfiguredBeanWithDefaults() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		RemotingService rs = (RemotingService) broker.getService("remoting-service");
		assertNotNull("Could not find the remoting service", rs);
		RemotingDestination rd = (RemotingDestination) rs.getDestination("annotatedRemoteBean1");
		assertNotNull("Destination not found", rd);
	}
	
	@SuppressWarnings("unchecked")
	public void testExportBeanWithCustomSettings() {
		broker = (MessageBroker) getApplicationContext().getBean("remoteServiceBroker", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		RemotingService rs = (RemotingService) broker.getService("remoting-service");
		assertNotNull("Could not find the remoting service", rs);
		RemotingDestination rd = (RemotingDestination) rs.getDestination("exportedAnnotatedRemoteBean2");
		assertNotNull("Destination not found", rd);
		String[] channels = new String[] {"my-amf", "my-secure-amf"};
		assertEquals("Channels not set",Arrays.asList(channels), rd.getChannels());
		
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
	
	public void testExportAnnotatedBeanWithDefaults() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		RemotingService rs = (RemotingService) broker.getService("remoting-service");
		assertNotNull("Could not find the remoting service", rs);
		RemotingDestination rd = (RemotingDestination) rs.getDestination("annotatedRemoteBean");
		assertNotNull("Destination not found", rd);
	}
	
	@FlexService
	public static class MyService1 {}
	
	@FlexService(value="exportedAnnotatedRemoteBean2", messageBroker="remoteServiceBroker", channels={"my-amf", "my-secure-amf"})
	public static class MyService2 {
		
		@FlexInclude
		public void foo(){}
		
		@FlexInclude
		public void bar(){}
		
		@FlexExclude
		public void zoo(){}
		
		@FlexExclude
		public void baz(){}
	}
}
