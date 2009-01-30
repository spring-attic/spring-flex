package org.springframework.flex.messaging.security;

import java.security.Principal;

import org.springframework.flex.messaging.AbstractMessageBrokerTests;
import org.springframework.security.AuthenticationException;
import org.springframework.security.MockAuthenticationManager;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.security.LoginManager;

public class SpringSecurityLoginCommandTests extends AbstractMessageBrokerTests {

	SpringSecurityLoginCommand cmd;
	
	public void testLoginCommandRegisteredWithDefaultConfig() throws Exception {
		
		cmd = new SpringSecurityLoginCommand(getMessageBroker(), new MockAuthenticationManager(true));
		
		cmd.afterPropertiesSet();
		
		LoginManager mgr = getMessageBroker().getLoginManager();
		assertTrue("LoginManager not started",mgr.isStarted());
		assertSame("SpringSecurityLoginCommand not set on the LoginManager",cmd, mgr.getLoginCommand());
		assertFalse("Should default to per session authentication", mgr.isPerClientAuthentication());
		
	}
	
	public void testLoginCommandRegisteredWithPerClientConfig() throws Exception {
		
		cmd = new SpringSecurityLoginCommand(getMessageBroker(), new MockAuthenticationManager(true));
		cmd.setPerClientAuthentication(true);
		cmd.afterPropertiesSet();
		
		LoginManager mgr = getMessageBroker().getLoginManager();
		assertTrue("LoginManager not started",mgr.isStarted());
		assertSame("SpringSecurityLoginCommand not set on the LoginManager",cmd, mgr.getLoginCommand());
		assertTrue("Should be set to per client authentication", mgr.isPerClientAuthentication());
		
	}
	
	public void testDoAuthentication_ValidLogin() throws Exception {
		String username = "foo";
		String password = "bar";
		
		cmd = new SpringSecurityLoginCommand(getMessageBroker(), new MockAuthenticationManager(true));
		
		Principal principal = cmd.doAuthentication(username, password);
		
		assertNotNull("A non-null Principal was not returned", principal);
		assertEquals(username, principal.getName());
	}
	
	public void testDoAuthentication_Failure() throws Exception {
		
		String username = "foo";
		String password = "bar";
		
		cmd = new SpringSecurityLoginCommand(getMessageBroker(), new MockAuthenticationManager(false));
		
		try {
			cmd.doAuthentication(username, password);
			fail("An AuthenticationException was not thrown");
		} catch (AuthenticationException ex) {
			//expected
		}
	}
	
	public void testLogoutWithDefaults() throws Exception {
		String username = "foo";
		String password = "bar";

		MockFlexSession session = new MockFlexSession();
		FlexContext.setThreadLocalSession(session);
		
		cmd = new SpringSecurityLoginCommand(getMessageBroker(), new MockAuthenticationManager(true));
		
		Principal principal = cmd.doAuthentication(username, password);
		
		SecurityContext original = SecurityContextHolder.getContext();
		
		cmd.logout(principal);
		
		assertTrue("SecurityContext was not cleared",original != SecurityContextHolder.getContext());
		assertFalse("FlexSession was not invalidated",session.isValid());
	}
	
	public void testLogoutWithInvalidateSessionFalse() throws Exception {
		String username = "foo";
		String password = "bar";

		FlexSession session = new MockFlexSession();
		FlexContext.setThreadLocalSession(session);
		
		cmd = new SpringSecurityLoginCommand(getMessageBroker(), new MockAuthenticationManager(true));
		cmd.setInvalidateFlexSession(false);
		
		Principal principal = cmd.doAuthentication(username, password);
		
		SecurityContext original = SecurityContextHolder.getContext();
		
		cmd.logout(principal);
		
		assertTrue("SecurityContext was not cleared",original != SecurityContextHolder.getContext());
		assertTrue("FlexSession was invalidated",session.isValid());
	}
	
	private static class MockFlexSession extends FlexSession {

		private boolean valid = true;
			
		@Override
		public String getId() {
			return "mockFlexSession";
		}

		@Override
		public boolean isPushSupported() {
			return false;
		}

		@Override
		public void invalidate() {
			valid = false;
		}

		public boolean isValid() {
			return valid;
		}
	}
	

}
