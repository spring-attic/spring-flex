/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.springframework.flex.config.BeanIds;
import org.springframework.flex.messaging.jms.JmsAdapter;
import org.springframework.test.context.ContextConfiguration;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.services.MessageService;
import static org.junit.Assert.*;
import org.junit.Test;

@ContextConfiguration("classpath:org/springframework/flex/config/message-destination.xml")
public class JmsMessageDestinationBeanDefinitionParserTests extends AbstractMessageDestinationBeanDefinitionParserTests {

    @Test
    public void customConnectionFactory() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination("jmsCustomConnectionFactoryDestination");
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());
        JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
        assertTrue(adapter.getJmsTemplate().getDefaultDestination() instanceof TestDestination);
        assertSame(applicationContext.getBean("customConnectionFactory"), adapter.getJmsTemplate().getConnectionFactory());
    }

    @Test
    public void destinationRef() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination("jmsCustomReferences");
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());
        JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
        assertTrue(adapter.getJmsTemplate().getDefaultDestination() instanceof TestDestination);
    }

    @Test
    public void queue() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination("jmsQueueDestination");
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());
        JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
        assertEquals("myJmsQueue", adapter.getJmsTemplate().getDefaultDestinationName());
    }

    @Test
    public void topic() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination("jmsTopicDestination");
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());
        JmsAdapter adapter = (JmsAdapter) destination.getAdapter();
        assertEquals("myJmsTopic", adapter.getJmsTemplate().getDefaultDestinationName());
    }

    @Override
    protected String getCustomConfigDestination() {
        return "jmsCustomConfig";
    }

    public static final class TestConnectionFactory implements ConnectionFactory {

        public Connection createConnection() throws JMSException {
            return null;
        }

        public Connection createConnection(String userName, String password) throws JMSException {
            return null;
        }

    }

    public static final class TestDestination implements Destination {
    }
    
}
