package org.springframework.flex.messaging.config;

import flex.messaging.MessageBroker;

public interface MessageBrokerConfigProcessor {
	
	public MessageBroker processBeforeStartup(MessageBroker broker);
	
	public MessageBroker processAfterStartup(MessageBroker broker);
}
