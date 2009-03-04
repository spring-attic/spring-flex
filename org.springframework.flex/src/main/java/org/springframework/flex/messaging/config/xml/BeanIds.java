package org.springframework.flex.messaging.config.xml;

/**
 * Default bean ids for beans configured with the XML namespace.
 * 
 * @author Jeremy Grelle
 *
 */
public abstract class BeanIds {
	
	public static final String MESSAGE_BROKER = "_messageBroker";

	static final String MESSAGE_BROKER_HANDLER_ADAPTER = "_messageBrokerHandlerAdapter";

	static final String HANDLER_MAPPING_SUFFIX = "DefaultHandlerMapping";

	static final String LOGIN_COMMAND_SUFFIX = "LoginCommand";

	static final String SECURITY_PROCESSOR_SUFFIX = "SecurityProcessor";

	static final String REMOTING_PROCESSOR_SUFFIX = "RemotingProcessor";
}
