package org.springframework.flex.messaging.security;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.springframework.flex.messaging.config.MessageBrokerConfigProcessor;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.security.LoginCommand;
import flex.messaging.security.LoginManager;

/**
 * Custom BlazeDS {@link LoginCommand} that uses Spring Security for Authentication and Authorization.
 * 
 * <p>
 * This LoginCommand should be configured as a Spring bean and given a reference to the current {@link AuthenticationManager}.
 * It must be added to the {@link MessageBrokerFactoryBean}'s list of {@link MessageBrokerConfigProcessor}s.
 * </p>
 * 
 * @author Jeremy Grelle
 *
 * @see org.springframework.flex.messaging.MessageBrokerFactoryBean
 */
public class SpringSecurityLoginCommand implements LoginCommand, MessageBrokerConfigProcessor{

	private AuthenticationManager authManager;
	
	private boolean invalidateFlexSession = true;

	public void setInvalidateFlexSession(boolean invalidateFlexSession) {
		this.invalidateFlexSession = invalidateFlexSession;
	}

	public void setPerClientAuthentication(boolean perClientAuthentication) {
		this.perClientAuthentication = perClientAuthentication;
	}

	private boolean perClientAuthentication = false;
	
	public SpringSecurityLoginCommand(AuthenticationManager authManager) {
		Assert.notNull(authManager, "AuthenticationManager is required.");
		this.authManager = authManager;
	}

	public Principal doAuthentication(String username, Object credentials) {
		Authentication authentication = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(username,
						extractPassword(credentials)));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		return authentication;
	}
	
	@SuppressWarnings("unchecked")
	public boolean doAuthorization(Principal principal, List roles) {
		Assert.isInstanceOf(Authentication.class, principal, "This LoginCommand expects a Principal of type "+Authentication.class.getName());
		Authentication auth = (Authentication) principal;
		if ((auth == null) || (auth.getPrincipal() == null) || (auth.getAuthorities() == null)) {
            return false;
        }
		
        for (int i = 0; i < auth.getAuthorities().length; i++) {
            if (roles.contains(auth.getAuthorities()[i].getAuthority())) {
                return true;
            }
        }
		return false;
	}

	public boolean logout(Principal principal) {
		if (invalidateFlexSession) {
			FlexContext.getFlexSession().invalidate();
		}
		SecurityContextHolder.clearContext();
		return true;
	}
	
	public void start(ServletConfig config) {
		//Nothing to do
	}

	public void stop() {
		SecurityContextHolder.clearContext();
	}
	
	public MessageBroker processAfterStartup(MessageBroker broker) {
		return broker;
	}

	public MessageBroker processBeforeStartup(MessageBroker broker) {
		LoginManager loginManager = broker.getLoginManager();
		loginManager.setLoginCommand(this);
		loginManager.setPerClientAuthentication(perClientAuthentication);
		return broker;
	}
	
	@SuppressWarnings("unchecked")
	protected String extractPassword(Object credentials)
    {
        String password = null;
        if (credentials instanceof String)
        {
            password = (String)credentials;
        }
        else if (credentials instanceof Map)
        {
            password = (String)((Map)credentials).get(MessageIOConstants.SECURITY_CREDENTIALS);
        }
        return password;
    }
}
