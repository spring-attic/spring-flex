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

import org.springframework.flex.config.AbstractFlexConfigurationTests;

import edu.emory.mathcs.backport.java.util.Arrays;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.services.MessageService;

public abstract class AbstractMessageDestinationBeanDefinitionParserTests extends AbstractFlexConfigurationTests {

    protected MessageBroker broker;

    @SuppressWarnings("unchecked")
    public void testDestination_CustomConfig() {
        this.broker = (MessageBroker) getApplicationContext().getBean("messageServiceBroker", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        MessageService ms = (MessageService) this.broker.getService("message-service");
        assertNotNull("Could not find the message service", ms);
        MessageDestination destination = (MessageDestination) ms.getDestination(getCustomConfigDestination());
        assertNotNull("destination not found", destination);
        assertNotNull("adapter not set", destination.getAdapter());

        String[] channels = new String[] { "my-polling-amf", "my-secure-amf" };
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

    protected abstract String getCustomConfigDestination();

    /**
     * Uncomment this only for faster dev time testing
     */
    // @Override
    // protected String[] getConfigLocations() {
    // return new String[] {"classpath:org/springframework/flex/config/message-destination.xml"};
    // }
}
