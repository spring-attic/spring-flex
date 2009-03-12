package org.springframework.flex.messaging;

import flex.messaging.MessageException;

/**
 * Helper interface that allows for translation of known exception types into BlazeDS MessageExceptions that
 * will propagate proper AMF error message back to the client.
 * 
 * <p>
 * Implementations are encouraged to set the code of the created MessageException to something more useful than
 * the generic "Server.Processing" so that the client may reason on the code for custom fault handling logic.
 * 
 * @author Jeremy Grelle
 *
 */
public interface ExceptionTranslator {

	MessageException translate(Throwable t);
	
	boolean handles(Class<?> clazz);
}
