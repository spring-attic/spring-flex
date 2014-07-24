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

import java.util.Arrays;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.cluster.ClusterManager;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.services.MessageService;

public abstract class AbstractMessageDestinationBeanDefinitionParserTests extends AbstractFlexConfigurationTests {

    protected MessageBroker broker;

    @Test
    public void customConfig() {
        this.broker = (MessageBroker) applicationContext.getBean("messageServiceBroker", MessageBroker.class);
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
        assertFalse(destination.getServerSettings().isDisallowWildcardSubtopics());
        assertTrue(destination.getServerSettings().isBroadcastRoutingMode());
        assertEquals("default-cluster", destination.getNetworkSettings().getClusterId());
        assertEquals(1, destination.getServerSettings().getMessageTTL());
        assertEquals(1, destination.getNetworkSettings().getSubscriptionTimeoutMinutes());
        assertEquals("/", destination.getServerSettings().getSubtopicSeparator());
        assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getIncomingDestinationFrequency());
        assertEquals(ThrottleSettings.parsePolicy("ERROR"), destination.getNetworkSettings().getThrottleSettings().getInboundPolicy());
        assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getOutgoingDestinationFrequency());
        assertEquals(ThrottleSettings.parsePolicy("IGNORE"), destination.getNetworkSettings().getThrottleSettings().getOutboundPolicy());

    }

    protected abstract String getCustomConfigDestination();
    
    public static final class ClusterManagerConfigProcessor implements MessageBrokerConfigProcessor {
    	
    	@Mock
		private ClusterManager clusterManager;
    	
    	public ClusterManagerConfigProcessor() {
    		MockitoAnnotations.initMocks(this);
    	}

		public MessageBroker processAfterStartup(MessageBroker broker) {
			ReflectionTestUtils.setField(broker, "clusterManager", this.clusterManager);
			return broker;
		}

		public MessageBroker processBeforeStartup(MessageBroker broker) {
			return broker;
		}
	}

    /**
     * Uncomment this only for faster dev time testing
     */
    // @Override
    // protected String[] getConfigLocations() {
    // return new String[] {"classpath:org/springframework/flex/config/message-destination.xml"};
    // }
}
