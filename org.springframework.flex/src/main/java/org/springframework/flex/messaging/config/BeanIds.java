package org.springframework.flex.messaging.config;

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
	
	public static final String SESSION_FIXATION_PROTECTION_CONFIGURER = "_sessionFixationProtectionConfigurer";
	
	public static final String REMOTING_ANNOTATION_PROCESSOR = "_flexRemotingAnnotationPostProcessor";
	
	public static final String MESSAGE_BROKER_HANDLER_ADAPTER = "_messageBrokerHandlerAdapter";

	public static final String HANDLER_MAPPING_SUFFIX = "DefaultHandlerMapping";

	public static final String LOGIN_COMMAND_SUFFIX = "LoginCommand";

	public static final String SECURITY_PROCESSOR_SUFFIX = "SecurityProcessor";

	public static final String REMOTING_PROCESSOR_SUFFIX = "RemotingProcessor";
}
