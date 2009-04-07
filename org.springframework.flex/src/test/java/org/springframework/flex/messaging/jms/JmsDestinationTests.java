/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.messaging.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.flex.messaging.MessageDestinationFactory;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.services.MessageService;

/**
 * @author Mark Fisher
 */
public class JmsDestinationTests extends AbstractMessageBrokerTests {

	private static final String DEFAULT_ID = "testJmsDestinationExporter";

	MessageDestinationFactory factory;


	public void setUp() throws Exception {
		configureFactory();
	}

	public void tearDown() throws Exception {
		factory.destroy();
	}


	public void testDestinationRegisteredWithDefaultConfig() throws Exception {
		MessageService messageService = getMessageService();
		factory.afterPropertiesSet();
		MessageDestination messageDestination = (MessageDestination) messageService.getDestination(DEFAULT_ID);
		assertNotNull("MessageDestination not registered", messageDestination);
		assertTrue("MessageDestination not started", messageDestination.isStarted());
		assertEquals("MessagingAdapter not set", "test-jms-adapter", messageDestination.getAdapter().getId());
		assertTrue("No channels set on destination", messageDestination.getChannels().size() > 0);
	}

	public void testDestinationRegisteredWithDestinationId() throws Exception {
		MessageService messageService = getMessageService();
		String destinationId = "myDestination";
		factory.setDestinationId(destinationId);
		factory.afterPropertiesSet();
		assertNotNull("MessageDestination not registered", messageService.getDestination(destinationId));
	}


	private MessageService getMessageService() throws Exception {
		MessageBroker broker = getMessageBroker();
		return (MessageService) broker.getServiceByType(MessageService.class.getName());
	}

	private void configureFactory() throws Exception {
		String adapterBeanName = "test-jms-adapter";
		MutablePropertyValues properties = new MutablePropertyValues();
		properties.addPropertyValue("connectionFactory", new StubConnectionFactory());
		properties.addPropertyValue("jmsDestination", new Destination() {});
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerPrototype(adapterBeanName, JmsAdapter.class, properties);

		factory = new MessageDestinationFactory();
		factory.setBeanName(DEFAULT_ID);
		factory.setAdapterBeanName(adapterBeanName);
		factory.setBeanFactory(context);
		factory.setMessageBroker(getMessageBroker());
	}


	private static class StubConnectionFactory implements ConnectionFactory {

		public Connection createConnection() throws JMSException {
			return null;
		}

		public Connection createConnection(String userName, String password) throws JMSException {
			return null;
		}

	}

}
