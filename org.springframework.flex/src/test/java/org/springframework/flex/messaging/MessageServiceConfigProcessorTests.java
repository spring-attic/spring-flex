package org.springframework.flex.messaging;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.core.AbstractMessageBrokerTests;

import flex.messaging.MessageBroker;
import flex.messaging.services.MessageService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class MessageServiceConfigProcessorTests extends AbstractMessageBrokerTests {

	private String servicesConfigPath;
	
	private @Mock BeanFactory beanFactory;
	
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	public void testMessageServiceExists() throws Exception {
		setDirty();
		MessageServiceChecker checker = new MessageServiceChecker();
		addStartupProcessor(checker);
		addStartupProcessor(new MessageServiceConfigProcessor());
		servicesConfigPath = super.getServicesConfigPath();
		
		getMessageBroker();
		
		assertTrue("Processors not invoked", checker.beforeInvoked && checker.afterInvoked);
		assertSame("Pre-configured MessageService should be unmodified",
				checker.beforeMessageService, checker.afterMessageService);
	}
	
	public void testMessageServiceAddedWithBrokerDefaultChannels() throws Exception {
		setDirty();
		addStartupProcessor(new MessageServiceConfigProcessor());
		servicesConfigPath = "classpath:org/springframework/flex/messaging/default-channels-config.xml";
		
		MessageService messageService = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());
	
		assertTrue("The broker's default channel was not assigned to the MessageService",messageService.getDefaultChannels().contains("my-default-amf"));
		assertEquals("The default adapter was not set", "actionscript", messageService.getDefaultAdapter());
	}
	
	public void testMessageServiceAddedWithInferredDefaultChannels() throws Exception {
		setDirty();
		addStartupProcessor(new MessageServiceConfigProcessor());
		servicesConfigPath = "classpath:org/springframework/flex/messaging/inferred-default-channels-config.xml";
		
		MessageService messageService = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());
		assertTrue("The default channel was not determined",messageService.getDefaultChannels().contains("my-inferred-default-amf"));
		assertEquals("The default adapter was not set", "actionscript", messageService.getDefaultAdapter());
	}
	
	public void testMessageServiceAddedWithCustomDefaults() throws Exception {
		setDirty();
		MessageServiceConfigProcessor processor = new MessageServiceConfigProcessor();
		processor.setBeanFactory(beanFactory);
		processor.setDefaultAdapterId("my-adapter");
		processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
		addStartupProcessor(processor);
		servicesConfigPath = "classpath:org/springframework/flex/messaging/default-channels-config.xml";
		
		when(beanFactory.getType("my-adapter")).thenReturn(TestAdapter.class);

		MessageService messageService = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());
		assertTrue("The default channel was not set",messageService.getDefaultChannels().contains("my-custom-default-amf"));
		assertEquals("The default adapter was not set", "my-adapter", messageService.getDefaultAdapter());
	}
	
	public void testMessageServiceAddedWithInvalidDefaultId() throws Exception {
		setDirty();
		MessageServiceConfigProcessor processor = new MessageServiceConfigProcessor();
		processor.setBeanFactory(beanFactory);
		processor.setDefaultAdapterId("my-adapter");
		processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
		addStartupProcessor(processor);
		servicesConfigPath = "classpath:org/springframework/flex/messaging/default-channels-config.xml";
		
		try {
			getMessageBroker().getServiceByType(MessageService.class.getName());
			fail("An error should be thrown.");
		} catch(IllegalArgumentException ex) {
			//expected
		}
	}
	
	public void testMessageServiceAddedWithInvalidCustomChannels() throws Exception {
		setDirty();
		MessageServiceConfigProcessor processor = new MessageServiceConfigProcessor();
		processor.setDefaultChannels(new String[] { "my-bogus-channel" });
		addStartupProcessor(processor);
		servicesConfigPath = "classpath:org/springframework/flex/messaging/default-channels-config.xml";
		
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
	
	private static class MessageServiceChecker implements MessageBrokerConfigProcessor {

		protected MessageService beforeMessageService;
		protected MessageService afterMessageService;
		protected boolean beforeInvoked = false;
		protected boolean afterInvoked = false;
		
		public MessageBroker processAfterStartup(MessageBroker broker) {
			afterInvoked = true;
			afterMessageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
			assertNotNull(afterMessageService);
			return broker;
		}

		public MessageBroker processBeforeStartup(MessageBroker broker) {
			beforeInvoked = true;
			beforeMessageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
			assertNotNull(beforeMessageService);
			return broker;
		}
		
	}
}
