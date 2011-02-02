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

package org.springframework.flex.security3;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.core.MessageBrokerFactoryBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import flex.messaging.FlexContext;
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
public class SpringSecurityLoginCommand implements LoginCommand, MessageBrokerConfigProcessor, InitializingBean {

    private final AuthenticationManager authManager;
    
    private List<LogoutHandler> logoutHandlers;
    
    private RememberMeServices rememberMeServices;

    private SessionAuthenticationStrategy sessionStrategy;
    
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
    
    public void afterPropertiesSet() throws Exception {		
		if (this.sessionStrategy == null) {
        	this.sessionStrategy = new NullAuthenticatedSessionStrategy();
        }
        if (this.rememberMeServices == null) {
        	this.rememberMeServices = new NullRememberMeServices();
        }
        if (this.logoutHandlers == null) {
        	this.logoutHandlers = new ArrayList<LogoutHandler>();
        }
        if (ClassUtils.isAssignableValue(LogoutHandler.class, this.rememberMeServices) && !this.logoutHandlers.contains(this.rememberMeServices)) {
        	this.logoutHandlers.add((LogoutHandler) this.rememberMeServices);
        }
	}

    /**
     * 
     * {@inheritDoc}
     */
    public Principal doAuthentication(String username, Object credentials) {
    	HttpServletRequest request = FlexContext.getHttpRequest();
    	HttpServletResponse response = FlexContext.getHttpResponse();
    	try {
	        Authentication authentication = this.authManager.authenticate(new UsernamePasswordAuthenticationToken(username, extractPassword(credentials)));
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        if (authentication != null && !isPerClientAuthentication()) {
	        	if (request != null && response != null) {
	        		this.sessionStrategy.onAuthentication(authentication, request, response);
	        		this.rememberMeServices.loginSuccess(request, response, authentication);
	        	}
	        }
	        return authentication;
    	} catch (AuthenticationException ex) {
    		if (request != null && response != null) {
    			this.rememberMeServices.loginFail(request, response);
    		}
    		throw ex;
    	}
    }

    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
	public boolean doAuthorization(Principal principal, List roles) {
        Assert.isInstanceOf(Authentication.class, principal, "This LoginCommand expects a Principal of type " + Authentication.class.getName());
        Authentication auth = (Authentication) principal;
        if (auth == null || auth.getPrincipal() == null || auth.getAuthorities() == null) {
            return false;
        }

        for (GrantedAuthority grantedAuthority : auth.getAuthorities()) {
            if (roles.contains(grantedAuthority.getAuthority())) {
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
    	
    	HttpServletRequest request = FlexContext.getHttpRequest();
    	HttpServletResponse response = FlexContext.getHttpResponse();
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
    	if (request != null && response != null) {
    		for (LogoutHandler handler : logoutHandlers) {
                handler.logout(request, response, auth);
            }
    	} else {
    		SecurityContextHolder.clearContext();
    	}
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

    public void setLogoutHandlers(List<LogoutHandler> logoutHandlers) {
		this.logoutHandlers = logoutHandlers;
	}

	/**
     * Configures the per-client authentication setting for the BlazeDS login manager
     * 
     * @param perClientAuthentication true if per-client authentication is enabled
     */
    public void setPerClientAuthentication(boolean perClientAuthentication) {
        this.perClientAuthentication = perClientAuthentication;
    }
    
    public void setRememberMeServices(RememberMeServices rememberMeServices) {
		this.rememberMeServices = rememberMeServices;		
	}
    
    public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionStrategy) {
		this.sessionStrategy = sessionStrategy;
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
    @SuppressWarnings("rawtypes")
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
