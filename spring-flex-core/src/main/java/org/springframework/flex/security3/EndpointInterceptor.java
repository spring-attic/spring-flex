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

import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * Security interceptor that secures messages being passed to BlazeDS endpoints based on the security attributes
 * configured for the endpoint being invoked.
 * 
 * @author Jeremy Grelle
 */
public class EndpointInterceptor extends AbstractSecurityInterceptor implements MessageInterceptor {

    private static final String STATUS_TOKEN = "_enpointInterceptorStatusToken";

    private EndpointSecurityMetadataSource securityMetadataSource;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (getAccessDecisionManager() == null) {
            configureDefaultAccessDecisionManager();
        }
        super.afterPropertiesSet();
    }

    public EndpointSecurityMetadataSource getObjectDefinitionSource() {
        return this.securityMetadataSource;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Class<?> getSecureObjectClass() {
        return AbstractEndpoint.class;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
        if (context.getAttributes().containsKey(STATUS_TOKEN)) {
            InterceptorStatusToken token = (InterceptorStatusToken) context.getAttributes().get(STATUS_TOKEN);
            return (Message) afterInvocation(token, outputMessage);
        } else {
            return outputMessage;
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Message preProcess(MessageProcessingContext context, Message inputMessage) {
        if (!isPassThroughCommand(inputMessage)) {
            InterceptorStatusToken token = beforeInvocation(context.getMessageTarget());
            context.getAttributes().put(STATUS_TOKEN, token);
        }
        return inputMessage;
    }

    /**
     * Sets the {@link EndpointSecurityMetadataSource} for the endpoint being secured
     * 
     * @param newSource the endpoint definition source
     */
    public void setObjectDefinitionSource(EndpointSecurityMetadataSource newSource) {
        this.securityMetadataSource = newSource;
    }

	private void configureDefaultAccessDecisionManager() {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
        voters.add(new RoleVoter());
        voters.add(new AuthenticatedVoter());
        AffirmativeBased adm = new AffirmativeBased(voters);
        setAccessDecisionManager(adm);
    }
    
    private boolean isPassThroughCommand(Message message) {
        if (message instanceof CommandMessage) {
            CommandMessage command = (CommandMessage) message;
            return command.getOperation() == CommandMessage.CLIENT_PING_OPERATION || command.getOperation() == CommandMessage.LOGIN_OPERATION;
        }
        return false;
    }
}
