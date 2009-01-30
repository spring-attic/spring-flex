package org.springframework.flex.messaging.security;

import java.security.Principal;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.security.AppServerLoginCommand;
import flex.messaging.security.LoginManager;

/**
 * Custom BlazeDS {@link LoginCommand} that uses Spring Security for Authentication and Authorization.
 * 
 * <p>
 * This LoginCommand should be configured as a Spring bean and given a reference to a Spring-managed {@link MessageBroker} and 
 * the current {@link AuthenticationManager}. 
 * </p>
 * 
 * <p>
 * This class is only needed if using BlazeDS-specific security mechanisms.  For example, when securing a remoting destination
 * by role using services-config.xml, or when manually calling login() or logout() on a ChannelSet in the Flex client. 
 * </p>
 * 
 * @author Jeremy Grelle
 *
 * @see org.springframework.flex.messaging.MessageBrokerFactoryBean
 */
public class SpringSecurityLoginCommand extends AppServerLoginCommand implements InitializingBean{

	private AuthenticationManager authManager;
	
	private MessageBroker messageBroker;
	
	private boolean invalidateFlexSession = true;

	public void setInvalidateFlexSession(boolean invalidateFlexSession) {
		this.invalidateFlexSession = invalidateFlexSession;
	}

	public void setPerClientAuthentication(boolean perClientAuthentication) {
		this.perClientAuthentication = perClientAuthentication;
	}

	private boolean perClientAuthentication = false;
	
	public SpringSecurityLoginCommand(MessageBroker messageBroker, AuthenticationManager authManager) {
		Assert.notNull(messageBroker, "MessageBroker is required.");
		Assert.notNull(authManager, "AuthenticationManager is required.");
		this.messageBroker = messageBroker;
		this.authManager = authManager;
	}

	public Principal doAuthentication(String username, Object credentials) {
		Authentication authentication = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(username,
						extractPassword(credentials)));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		return authentication;
	}

	public boolean logout(Principal principal) {
		if (invalidateFlexSession) {
			FlexContext.getFlexSession().invalidate();
		}
		SecurityContextHolder.clearContext();
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		
		LoginManager loginManager = messageBroker.getLoginManager();
		loginManager.setLoginCommand(this);
		loginManager.setPerClientAuthentication(perClientAuthentication);
		loginManager.start();
		
	}
}
