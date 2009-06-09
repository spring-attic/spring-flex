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

import java.util.HashMap;
import java.util.Map;

import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * {@link MessageInterceptor} implementation that replaces the standard login success message with one that contains
 * relevant information about the logged in user.
 * 
 * <p>
 * The body of the returned message will contain the following properties as obtained from the {@link Authentication}
 * object:
 * <ul>
 * <li>name - the "name" property from the authentication</li>
 * <li>authorities - an array of String representations of the authentication's authorities (i.e. obtained through
 * {@link GrantedAuthority#getAuthority})</li>
 * </ul>
 * 
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

    private Map<String, Object> getAuthenticationResult() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> authenticationResult = new HashMap<String, Object>();
        authenticationResult.put("name", authentication.getName());
        String[] authorities = new String[authentication.getAuthorities().length];
        for (int i = 0; i < authorities.length; i++) {
            authorities[i] = authentication.getAuthorities()[i].getAuthority();
        }
        authenticationResult.put("authorities", authorities);
        return authenticationResult;
    }

}
