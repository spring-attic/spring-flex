package org.springframework.flex.messaging;

import flex.messaging.MessageBroker;

public interface MessageBrokerStartupProcessor {
	
	public MessageBroker processBeforeStartup(MessageBroker broker);
	
	public MessageBroker processAfterStartup(MessageBroker broker);
}
