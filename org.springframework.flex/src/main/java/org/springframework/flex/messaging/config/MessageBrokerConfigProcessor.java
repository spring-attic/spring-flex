package org.springframework.flex.messaging.config;

import flex.messaging.MessageBroker;

/**
 * Factory hook that allows for custom configuration of a MessageBroker when created
 * by a {@link MessageBrokerFactoryBean}.     
 * 
 * @author Jeremy Grelle
 * @see MessageBrokerFactoryBean
 */
public interface MessageBrokerConfigProcessor {
	
	/**
	 * Apply this config processor to the newly created MessageBroker after its intial configuration
	 * settings have been parsed from the BlazeDS XML configuration, but before it has actually been
	 * started.
	 * 
	 * @param broker the new MessageBroker instance
	 * @return the modified MessageBroker
	 */
	public MessageBroker processBeforeStartup(MessageBroker broker);
	
	/**
	 * Apply this config processor to the newly created MessageBroker after it and all of its services
	 * have been started but before it is available for use.  
	 * 
	 * @param broker the started MessageBroker instance
	 * @return the modified MessageBroker
	 */
	public MessageBroker processAfterStartup(MessageBroker broker);
}
