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

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.core.MessageBrokerFactoryBean;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

import flex.messaging.MessageBroker;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.security.LoginCommand;
import flex.messaging.security.LoginManager;

/**
 * Custom BlazeDS {@link LoginCommand} that uses Spring Security for Authentication and Authorization.
 * 
 * <p>
 * Should be configured as a Spring bean and given a reference to the current {@link AuthenticationManager}. It must be
 * added to the {@link MessageBrokerFactoryBean}'s list of {@link MessageBrokerConfigProcessor}s.
 * 
 * <p>
 * Will be configured automatically when using the <code>secured</code> tag in the xml config namespace.
 * 
 * @author Jeremy Grelle
 * 
 * @see org.springframework.flex.core.MessageBrokerFactoryBean
 */
public class SpringSecurityLoginCommand implements LoginCommand, MessageBrokerConfigProcessor {

    private final AuthenticationManager authManager;

    private boolean perClientAuthentication = false;

    /**
     * Creates a new SpringSecurityLoginCommand with the provided {@link AuthenticationManager}
     * 
     * @param authManager the authentication manager
     */
    public SpringSecurityLoginCommand(AuthenticationManager authManager) {
        Assert.notNull(authManager, "AuthenticationManager is required.");
        this.authManager = authManager;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Principal doAuthentication(String username, Object credentials) {
        Authentication authentication = this.authManager.authenticate(new UsernamePasswordAuthenticationToken(username, extractPassword(credentials)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public boolean doAuthorization(Principal principal, List roles) {
        Assert.isInstanceOf(Authentication.class, principal, "This LoginCommand expects a Principal of type " + Authentication.class.getName());
        Authentication auth = (Authentication) principal;
        if (auth == null || auth.getPrincipal() == null || auth.getAuthorities() == null) {
            return false;
        }

        for (int i = 0; i < auth.getAuthorities().length; i++) {
            if (roles.contains(auth.getAuthorities()[i].getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Spring Security {@link AuthenticationManager}
     * 
     * @return the authentication manager
     */
    public AuthenticationManager getAuthManager() {
        return this.authManager;
    }

    /**
     * Checks whether per-client authentication is enabled
     * 
     * @return true is per-client authentication is enabled
     */
    public boolean isPerClientAuthentication() {
        return this.perClientAuthentication;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean logout(Principal principal) {
        SecurityContextHolder.clearContext();
        return true;
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
        loginManager.setLoginCommand(this);
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

    /**
     * 
     * {@inheritDoc}
     */
    public void start(ServletConfig config) {
        // Nothing to do
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void stop() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Extracts the password from the Flex client credentials
     * 
     * @param credentials the Flex client credentials
     * @return the extracted password
     */
    @SuppressWarnings("unchecked")
    protected String extractPassword(Object credentials) {
        String password = null;
        if (credentials instanceof String) {
            password = (String) credentials;
        } else if (credentials instanceof Map) {
            password = (String) ((Map) credentials).get(MessageIOConstants.SECURITY_CREDENTIALS);
        }
        return password;
    }
}
