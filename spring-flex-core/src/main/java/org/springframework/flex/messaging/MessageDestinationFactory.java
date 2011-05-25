/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.flex.messaging;

import org.springframework.flex.core.AbstractDestinationFactory;
import org.springframework.flex.core.MessageBrokerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.MessageService;
import flex.messaging.services.Service;

/**
 * A factory for Flex MessageDestinations that can be configured with a Spring-managed MessagingAdapter instance.
 * 
 * <p>
 * The destination will be exposed to the Flex client as a BlazeDS {@link MessageDestination}. By default, the id of the
 * destination will be the same as the bean name of this factory. This may be overridden using the
 * {@link #setDestinationId(String) 'destinationId'} property.
 * </p>
 * 
 * @see MessageBrokerFactoryBean
 * 
 * @author Mark Fisher
 * @author Jeremy Grelle
 */
public class MessageDestinationFactory extends AbstractDestinationFactory {

    private final ConfigMap properties;

    /**
     * Creates a new MessageDestinationFactory
     */
    public MessageDestinationFactory() {
        this.properties = new ConfigMap();
    }

    /**
     * Creates a new MessageDestinationFactory with the specified properties {@link ConfigMap}
     * 
     * @param properties the properties map for the destination
     */
    public MessageDestinationFactory(ConfigMap properties) {
        this.properties = properties;
    }

    /**
     * Returns the properties {@link ConfigMap} for the destination
     * 
     * @return the properties map
     */
    public ConfigMap getProperties() {
        return this.properties;
    }

    public void setAllowSubtopics(String allow) {
        getServerMap().addProperty("allow-subtopics", allow);
    }

    public void setClusterMessageRouting(String routingType) {
        getServerMap().addProperty("cluster-message-routing", routingType);
    }
    
    public void setClusterRef(String clusterRef) {
		getClusterMap().addProperty("ref", clusterRef);
	}
    
    public void setDisallowWildcardSubtopics(String disallow) {
        getServerMap().addProperty("disallow-wildcard-subtopics", disallow);
    }

    public void setMessageTimeToLive(String timeToLive) {
        getServerMap().addProperty("message-time-to-live", timeToLive);
    }

    public void setSendSecurityConstraint(String constraint) {
        ConfigMap constraintMap = new ConfigMap();
        constraintMap.addProperty("ref", constraint);
        getServerMap().addProperty("send-security-constraint", constraintMap);
    }

    public void setSubscribeSecurityConstraint(String constraint) {
        ConfigMap constraintMap = new ConfigMap();
        constraintMap.addProperty("ref", constraint);
        getServerMap().addProperty("subscribe-security-constraint", constraintMap);
    }

    public void setSubscriptionTimeoutMinutes(String timeout) {
        getNetworkMap().addProperty("subscription-timeout-minutes", timeout);
    }

    public void setSubtopicSeparator(String separator) {
        getServerMap().addProperty("subtopic-separator", separator);
    }

    public void setThrottleInboundMaxFrequency(String maxFrequency) {
        getThrottleInboundMap().addProperty("max-frequency", maxFrequency);
    }

    public void setThrottleInboundPolicy(String policy) {
        getThrottleInboundMap().addProperty("policy", policy);
    }

    public void setThrottleOutboundMaxFrequency(String maxFrequency) {
        getThrottleOutboundMap().addProperty("max-frequency", maxFrequency);
    }

    public void setThrottleOutboundPolicy(String policy) {
        getThrottleOutboundMap().addProperty("policy", policy);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected Destination createDestination(String destinationId, MessageBroker broker) throws Exception {
        MessageService messageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
        Assert.notNull(messageService, "Could not find a proper MessageService in the Flex MessageBroker.");
        MessageDestination destination = (MessageDestination) messageService.createDestination(destinationId);
        return destination;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void destroyDestination(String destinationId, MessageBroker broker) {
        MessageService messageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
        if (messageService == null) {
            return;
        }
        messageService.removeDestination(destinationId);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected Service getTargetService(MessageBroker broker) {
        return broker.getServiceByType(MessageService.class.getName());
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void initializeDestination(Destination destination) {
        String adapterId = StringUtils.hasText(destination.getAdapter().getId()) ? destination.getAdapter().getId() : getDestinationId() + "Adapter";
        
        //As per FLEX-198, we're doing the opposite of the BlazeDS default and defaulting disallow-wildcard-subtopics to "true"
        if (!getServerMap().containsKey("disallow-wildcard-subtopics")) {
            getServerMap().addProperty("disallow-wildcard-subtopics", "true");
        }
        
        destination.getAdapter().initialize(adapterId, getProperties());
        destination.initialize(getDestinationId(), getProperties());
        destination.start();
    }

    private ConfigMap getNetworkMap() {
        ConfigMap network = getProperties().getPropertyAsMap("network", null);
        if (network == null) {
            network = new ConfigMap();
            getProperties().addProperty("network", network);
        }
        return network;
    }

    private ConfigMap getServerMap() {
        ConfigMap server = getProperties().getPropertyAsMap("server", null);
        if (server == null) {
            server = new ConfigMap();
            getProperties().addProperty("server", server);
        }
        return server;
    }
    
    private ConfigMap getClusterMap() {
    	ConfigMap cluster = getNetworkMap().getPropertyAsMap("cluster", null);
    	if (cluster == null) {
    		cluster = new ConfigMap();
    		getNetworkMap().addProperty("cluster", cluster);
    	}
    	return cluster;
    }

    private ConfigMap getThrottleInboundMap() {
        ConfigMap throttleInbound = getNetworkMap().getPropertyAsMap("throttle-inbound", null);
        if (throttleInbound == null) {
            throttleInbound = new ConfigMap();
            getNetworkMap().addProperty("throttle-inbound", throttleInbound);
        }
        return throttleInbound;
    }

    private ConfigMap getThrottleOutboundMap() {
        ConfigMap throttleOutbound = getNetworkMap().getPropertyAsMap("throttle-outbound", null);
        if (throttleOutbound == null) {
            throttleOutbound = new ConfigMap();
            getNetworkMap().addProperty("throttle-outbound", throttleOutbound);
        }
        return throttleOutbound;
    }
}
