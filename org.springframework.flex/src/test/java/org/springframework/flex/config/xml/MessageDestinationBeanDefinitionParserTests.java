package org.springframework.flex.config.xml;

import org.springframework.flex.config.BeanIds;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.messages.Message;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

public class MessageDestinationBeanDefinitionParserTests extends
		AbstractMessageDestinationBeanDefinitionParserTests {

	
	public void testNativeDestination_DefaultConfig() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("nativeDefaultConfig");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		assertTrue(destination.getAdapter() instanceof ActionScriptAdapter);
	}
	
	public void testNativeDestination_CustomAdapter() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("nativeCustomAdapter");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		assertTrue(destination.getAdapter() instanceof TestMessagingAdapter);
	}
	
	@Override
	protected String getCustomConfigDestination() {
		return "nativeCustomConfig";
	}

	public static final class TestMessagingAdapter extends MessagingAdapter {
		@Override
		public Object invoke(Message message) {
			return null;
		}		
	}
}
