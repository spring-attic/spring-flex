package org.springframework.flex.messaging.remoting;

import org.springframework.flex.messaging.AbstractMessageBrokerTests;
import org.springframework.flex.messaging.config.MessageBrokerConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class RemotingServiceConfigProcessorTests extends AbstractMessageBrokerTests {

	private String servicesConfigPath;
	
	public void testRemotingServiceExists() throws Exception {
		setDirty();
		RemotingServiceChecker checker = new RemotingServiceChecker();
		addStartupProcessor(checker);
		addStartupProcessor(new RemotingServiceConfigProcessor());
		servicesConfigPath = super.getServicesConfigPath();
		
		getMessageBroker();
		
		assertTrue("Processors not invoked", checker.beforeInvoked && checker.afterInvoked);
		assertSame("Pre-configured RemotingService should be unmodified",
				checker.beforeRemotingService, checker.afterRemotingService);
	}
	
	public void testRemotingServiceAddedWithBrokerDefaultChannels() throws Exception {
		setDirty();
		addStartupProcessor(new RemotingServiceConfigProcessor());
		servicesConfigPath = "classpath:org/springframework/flex/messaging/remoting/default-channels-config.xml";
		
		RemotingService remotingService = (RemotingService) getMessageBroker().getServiceByType(RemotingService.class.getName());
	
		assertTrue("The broker's default channel was not assigned to the RemotingService",remotingService.getDefaultChannels().contains("my-default-amf"));
		assertEquals("The default adapter was not set", "java-adapter", remotingService.getDefaultAdapter());
	}
	
	public void testRemotingServiceAddedWithInferredDefaultChannels() throws Exception {
		setDirty();
		addStartupProcessor(new RemotingServiceConfigProcessor());
		servicesConfigPath = "classpath:org/springframework/flex/messaging/remoting/inferred-default-channels-config.xml";
		
		RemotingService remotingService = (RemotingService) getMessageBroker().getServiceByType(RemotingService.class.getName());
		assertTrue("The default channel was not determined",remotingService.getDefaultChannels().contains("my-inferred-default-amf"));
		assertEquals("The default adapter was not set", "java-adapter", remotingService.getDefaultAdapter());
	}
	
	public void testRemotingServiceAddedWithCustomDefaults() throws Exception {
		setDirty();
		RemotingServiceConfigProcessor processor = new RemotingServiceConfigProcessor();
		processor.setDefaultAdapterClass(TestAdapter.class.getName());
		processor.setDefaultAdapterId("my-adapter");
		processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
		addStartupProcessor(processor);
		servicesConfigPath = "classpath:org/springframework/flex/messaging/remoting/default-channels-config.xml";

		RemotingService remotingService = (RemotingService) getMessageBroker().getServiceByType(RemotingService.class.getName());
		assertTrue("The default channel was not set",remotingService.getDefaultChannels().contains("my-custom-default-amf"));
		assertEquals("The default adapter was not set", "my-adapter", remotingService.getDefaultAdapter());
	}
	
	public void testRemotingServiceAddedWithInvalidCustomChannels() throws Exception {
		setDirty();
		RemotingServiceConfigProcessor processor = new RemotingServiceConfigProcessor();
		processor.setDefaultChannels(new String[] { "my-bogus-channel" });
		addStartupProcessor(processor);
		servicesConfigPath = "classpath:org/springframework/flex/messaging/remoting/default-channels-config.xml";
		
		try {
			getMessageBroker();
			fail("Invalid channels not detected");
		} catch(IllegalArgumentException ex) {
			//expected
			setDirty();
		}
	}
	
	protected String getServicesConfigPath() {
		return servicesConfigPath;
	}
	
	private static class TestAdapter extends JavaAdapter {}
	
	private static class RemotingServiceChecker implements MessageBrokerConfigProcessor {

		protected RemotingService beforeRemotingService;
		protected RemotingService afterRemotingService;
		protected boolean beforeInvoked = false;
		protected boolean afterInvoked = false;
		
		public MessageBroker processAfterStartup(MessageBroker broker) {
			afterInvoked = true;
			afterRemotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());
			assertNotNull(afterRemotingService);
			return broker;
		}

		public MessageBroker processBeforeStartup(MessageBroker broker) {
			beforeInvoked = true;
			beforeRemotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());
			assertNotNull(beforeRemotingService);
			return broker;
		}
		
	}
}
