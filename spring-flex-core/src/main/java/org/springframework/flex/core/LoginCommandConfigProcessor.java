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

package org.springframework.flex.core;

import org.springframework.flex.config.MessageBrokerConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.security.LoginCommand;
import flex.messaging.security.LoginManager;

/**
 * {@link MessageBrokerConfigProcessor} implementation that is responsible for configuring the 
 * current {@link MessageBroker}'s {@link LoginManager} with a Spring-managed {@link LoginCommand}.
 *
 * @author Jeremy Grelle
 */
public class LoginCommandConfigProcessor implements MessageBrokerConfigProcessor {

	private final LoginCommand loginCommand;
	
	private boolean perClientAuthentication = false;
	
	public LoginCommandConfigProcessor(LoginCommand loginCommand) {
		this.loginCommand = loginCommand;
	}
	
	/**
     * 
     * {@inheritDoc}
     */
    public MessageBroker processAfterStartup(MessageBroker broker) {
        return broker;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public MessageBroker processBeforeStartup(MessageBroker broker) {
        LoginManager loginManager = broker.getLoginManager();
        loginManager.setLoginCommand(this.loginCommand);
        loginManager.setPerClientAuthentication(this.perClientAuthentication);
        return broker;
    }

    /**
     * Configures the per-client authentication setting for the BlazeDS login manager
     * 
     * @param perClientAuthentication true if per-client authentication is enabled
     */
	public void setPerClientAuthentication(boolean perClientAuthentication) {
		this.perClientAuthentication = perClientAuthentication;
	}
}
