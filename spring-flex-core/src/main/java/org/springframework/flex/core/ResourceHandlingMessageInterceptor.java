
package org.springframework.flex.core;

import flex.messaging.messages.Message;

/**
 * Extension of the {@link MessageInterceptor} interface that adds an additional callback to be invoked after standard
 * message processing is completed. This callback will be invoked even when an {@link Exception} is thrown by the
 * current request's {@link Endpoint}. This gives the interceptor a chance to clean up any resources that may have been
 * created for the current request.
 * 
 * @author Jeremy Grelle
 * @since 1.0.2
 */
public interface ResourceHandlingMessageInterceptor extends MessageInterceptor {

    /**
     * Callback to be invoked when message processing is complete, giving the interceptor a chance to clean up resources.
     * 
     * @param context - the {@link MessageProcessingContext} for the current request
     * @param inputMessage - the incoming AMF message
     * @param outputMessage - the outgoing AMF message, null in the case of an exception thrown by the {@link Endpoint}
     * @param ex - exception thrown by the {@link Endpoint}, null in the case of successful processing
     */
    public void afterCompletion(MessageProcessingContext context, Message inputMessage, Message outputMessage, Exception ex);
}
