package org.springframework.flex.messaging.config.xml;

/**
 * Default bean ids for beans configured with the XML namespace.
 * 
 * @author Jeremy Grelle
 *
 */
public abstract class BeanIds {
	
	public static final String MESSAGE_BROKER = "_messageBroker";

	public static final String FLEX_SESSION_AUTHENTICATION_LISTENER = "_flexSessionAuthenticationListener";
	
	public static final String REQUEST_CONTEXT_FILTER = "_requestContextFilter";
	
	static final String SESSION_FIXATION_PROTECTION_CONFIGURER = "_sessionFixationProtectionConfigurer";
	
	static final String MESSAGE_BROKER_HANDLER_ADAPTER = "_messageBrokerHandlerAdapter";

	static final String HANDLER_MAPPING_SUFFIX = "DefaultHandlerMapping";

	static final String LOGIN_COMMAND_SUFFIX = "LoginCommand";

	static final String SECURITY_PROCESSOR_SUFFIX = "SecurityProcessor";

	static final String REMOTING_PROCESSOR_SUFFIX = "RemotingProcessor";
}
