package org.springframework.flex.messaging;

import org.springframework.util.StringUtils;

import flex.messaging.Destination;
import flex.messaging.config.ConfigMap;

public class SimpleMessageDestinationFactory extends
		MessageDestinationFactory {

	private final ConfigMap properties;
	
	public SimpleMessageDestinationFactory() {
		this.properties = new ConfigMap();
	}
	
	public SimpleMessageDestinationFactory(ConfigMap properties) {
		this.properties = properties;
	}
	
	public void setSubscriptionTimeoutMinutes(String timeout) {
		getNetworkMap().addProperty("subscription-timeout-minutes", timeout);
	}
	
	public void setThrottleInboundPolicy(String policy) {
		getThrottleInboundMap().addProperty("policy", policy);
	}
	
	public void setThrottleInboundMaxFrequency(String maxFrequency) {
		getThrottleInboundMap().addProperty("max-frequency", maxFrequency);
	}
	
	public void setThrottleOutboundPolicy(String policy) {
		getThrottleOutboundMap().addProperty("policy", policy);
	}
	
	public void setThrottleOutboundMaxFrequency(String maxFrequency) {
		getThrottleOutboundMap().addProperty("max-frequency", maxFrequency);
	}
	
	public void setAllowSubtopics(String allow) {
		getServerMap().addProperty("allow-subtopics", allow);
	}
	
	public void setClusterMessageRouting(String routingType) {
		getServerMap().addProperty("cluster-message-routing", routingType);
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
	
	public void setSubtopicSeparator(String separator) {
		getServerMap().addProperty("subtopic-separator", separator);
	}

	@Override
	protected void initializeDestination(Destination destination) {
		String adapterId = StringUtils.hasText(destination.getAdapter().getId()) ? destination.getAdapter().getId() : getDestinationId()+"Adapter";
		destination.getAdapter().initialize(adapterId, properties);
		destination.initialize(getDestinationId(), properties);
		super.initializeDestination(destination);
	}
	
	private ConfigMap getNetworkMap() {
		ConfigMap network = properties.getPropertyAsMap("network", null);
		if (network == null) {
			network = new ConfigMap();
			properties.addProperty("network", network);
		}
		return network;
	}
	
	private ConfigMap getServerMap() {
		ConfigMap server = properties.getPropertyAsMap("server", null);
		if (server == null) {
			server = new ConfigMap();
			properties.addProperty("server", server);
		}
		return server;
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
