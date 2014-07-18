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

package org.springframework.flex.hibernate3.config.xml;

import org.springframework.flex.config.BeanIds;
import org.springframework.flex.messaging.integration.IntegrationAdapter;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.test.context.ContextConfiguration;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.services.MessageService;

@ContextConfiguration("classpath:org/springframework/flex/hibernate3/config/message-destination.xml")
public class IntegrationMessageDestinationBeanDefinitionParserTests extends AbstractMessageDestinationBeanDefinitionParserTests {

    public void testIntegrationDestination_SimpleConfig() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
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
