/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;

import org.springframework.flex.config.BeanIds;
import org.springframework.test.context.ContextConfiguration;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.messages.Message;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

@ContextConfiguration("classpath:org/springframework/flex/config/message-destination.xml")
public class MessageDestinationBeanDefinitionParserTests extends AbstractMessageDestinationBeanDefinitionParserTests {

    @Test
    public void customAdapter() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination("nativeCustomAdapter");
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());
        assertTrue(destination.getAdapter() instanceof TestMessagingAdapter);
    }

    @Test
    public void defaultConfig() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination("nativeDefaultConfig");
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());
        assertTrue(destination.getAdapter() instanceof ActionScriptAdapter);
        assertTrue(destination.getServerSettings().isDisallowWildcardSubtopics());
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
