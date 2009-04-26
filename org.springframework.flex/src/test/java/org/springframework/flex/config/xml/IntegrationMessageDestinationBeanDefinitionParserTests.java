package org.springframework.flex.config.xml;

import org.springframework.flex.config.BeanIds;
import org.springframework.flex.messaging.integration.IntegrationAdapter;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.services.MessageService;

public class IntegrationMessageDestinationBeanDefinitionParserTests extends
		AbstractMessageDestinationBeanDefinitionParserTests {

	public void testIntegrationDestination_SimpleConfig() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("integrationSimpleConfig");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		assertTrue(destination.getAdapter() instanceof IntegrationAdapter);
	}
	
	@Override
	protected String getCustomConfigDestination() {
		return "integrationCustomConfig";
	}
	
	public static final class TestMessageChannel implements MessageChannel {

		public String getName() {
			return null;
		}

		public boolean send(Message<?> message) {
			return false;
		}

		public boolean send(Message<?> message, long timeout) {
			return false;
		}
		
	} 

}
