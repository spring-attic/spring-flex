package org.springframework.flex.config.xml;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.springframework.flex.config.BeanIds;
import org.springframework.flex.messaging.jms.JmsAdapter;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.services.MessageService;

public class JmsMessageDestinationBeanDefinitionParserTests extends AbstractMessageDestinationBeanDefinitionParserTests {

	public void testJmsDestination_DestinationRef() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("jmsDestinationRef");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
		assertTrue(adapter.getJmsTemplate().getDefaultDestination() instanceof TestDestination);
	}
	
	public void testJmsDestination_CustomConnectionFactory() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("jmsCustomConnectionFactoryDestination");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
		assertTrue(adapter.getJmsTemplate().getDefaultDestination() instanceof TestDestination);
		assertSame(getApplicationContext().getBean("customConnectionFactory"), adapter.getJmsTemplate().getConnectionFactory());
	}
	
	public void testJmsDestination_Queue() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("jmsQueueDestination");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
		assertEquals("myJmsQueue",adapter.getJmsTemplate().getDefaultDestinationName());
	}
	
	public void testJmsDestination_Topic() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		MessageService ms = (MessageService) broker.getService("message-service");
		assertNotNull("Could not find the message service", ms);
		MessageDestination destination = (MessageDestination) ms.getDestination("jmsTopicDestination");
		assertNotNull("destination not found", destination);
		assertNotNull("adapter not set", destination.getAdapter());
		JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
		assertEquals("myJmsTopic",adapter.getJmsTemplate().getDefaultDestinationName());
	}

	@Override
	protected String getCustomConfigDestination() {
		return "jmsCustomConfig";
	}

	public static final class TestConnectionFactory implements ConnectionFactory {

		public Connection createConnection() throws JMSException {
			return null;
		}

		public Connection createConnection(String userName, String password)
				throws JMSException {
			return null;
		}
		
	}
	
	public static final class TestDestination implements Destination { }
}
