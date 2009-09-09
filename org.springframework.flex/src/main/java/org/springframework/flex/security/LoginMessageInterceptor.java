/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.flex.security;


import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;

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
                outputMessage.setBody(AuthenticationResultUtils.getAuthenticationResult());
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

}
