package org.springframework.flex.config.xml;

import org.springframework.flex.config.AbstractFlexConfigurationTests;

import edu.emory.mathcs.backport.java.util.Arrays;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.services.MessageService;

public abstract class AbstractMessageDestinationBeanDefinitionParserTests
		extends AbstractFlexConfigurationTests {

	protected MessageBroker broker;

	protected abstract String getCustomConfigDestination();

	@SuppressWarnings("unchecked")
	public void testDestination_CustomConfig() {
		broker = (MessageBroker) getApplicationContext().getBean("messageServiceBroker", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination(getCustomConfigDestination());
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		
		String[] channels = new String[] {"my-polling-amf", "my-secure-amf"};
		assertEquals(getCustomConfigDestination(), destination.getId());
		assertTrue(destination.getChannels().containsAll(Arrays.asList(channels)));
		assertTrue(destination.getServerSettings().getAllowSubtopics());
		assertTrue(destination.getServerSettings().isBroadcastRoutingMode());
		assertEquals(1, destination.getServerSettings().getMessageTTL());
		assertEquals(1, destination.getNetworkSettings().getSubscriptionTimeoutMinutes());
		assertEquals("/", destination.getServerSettings().getSubtopicSeparator());
		assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getIncomingDestinationFrequency());
		assertEquals(ThrottleSettings.POLICY_ERROR, destination.getNetworkSettings().getThrottleSettings().getInboundPolicy());
		assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getOutgoingDestinationFrequency());
		assertEquals(ThrottleSettings.POLICY_IGNORE, destination.getNetworkSettings().getThrottleSettings().getOutboundPolicy());
	
	}
	
	/**
	 * Uncomment this only for faster dev time testing
	 */
//	@Override
//	protected String[] getConfigLocations() {
//		return new String[] {"classpath:org/springframework/flex/config/message-destination.xml"};
//	}

}