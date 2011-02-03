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

package org.springframework.flex.security3;


import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * {@link MessageInterceptor} implementation that replaces the standard login success message with one that contains
 * relevant information about the logged in user.
 * 
 * @see AuthenticationResultUtils
 * @author Jeremy Grelle
 */
public class LoginMessageInterceptor implements MessageInterceptor {

    private static final String SUCCESS_MSG = "success";

    /**
     * 
     * {@inheritDoc}
     */
    public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
        if (inputMessage instanceof CommandMessage && ((CommandMessage) inputMessage).getOperation() == CommandMessage.LOGIN_OPERATION) {
            if (SUCCESS_MSG.equals(outputMessage.getBody())) {
                outputMessage.setBody(getAuthenticationResult());
            }
        }
        return outputMessage;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Message preProcess(MessageProcessingContext context, Message inputMessage) {
        return inputMessage;
    }
    
    /**
     * Converts the current {@link Authentication} object into a format suitable for AMF serialization back to the calling client.
     * 
     * <p>This is an intended extension point for providing a custom conversion strategy, for example, to additionally provide 
     * information from a {@link UserDetails} object.  The default implementation calls {@link AuthenticationResultUtils#getAuthenticationResult()} 
     * to convert the <code>Authentication</code> into a simple Map of the user's principal and granted authorities.  A more ambitious 
     * implementation might map onto a custom domain object for which there is a parallel ActionScript object on the client.
     * 
     * <p>To provide such a custom strategy, one would extend this class and replace the default implementation in the {@link MessageInterceptor} chain by 
     * utilizing the <code>position</code> attribute of the <code>message-interceptor</code> configuration tag.
     * 
     * @return pertinent details of the currently authenticated user
     */
    protected Object getAuthenticationResult(){
    	return AuthenticationResultUtils.getAuthenticationResult();
    }
}
