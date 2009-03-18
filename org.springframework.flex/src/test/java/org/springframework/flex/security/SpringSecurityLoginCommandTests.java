package org.springframework.flex.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.flex.security.SpringSecurityLoginCommand;
import org.springframework.security.AuthenticationException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.MockAuthenticationManager;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.security.LoginManager;

public class SpringSecurityLoginCommandTests extends AbstractMessageBrokerTests {

	SpringSecurityLoginCommand cmd;
	
	public void testLoginCommandRegisteredWithDefaultConfig() throws Exception {
		
		setDirty();
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
		
		addStartupProcessor(cmd);
		
		LoginManager mgr = getMessageBroker().getLoginManager();
		assertTrue("LoginManager not started",mgr.isStarted());
		assertSame("SpringSecurityLoginCommand not set on the LoginManager",cmd, mgr.getLoginCommand());
		assertFalse("Should default to per session authentication", mgr.isPerClientAuthentication());
		
	}
	
	public void testLoginCommandRegisteredWithPerClientConfig() throws Exception {
		
		setDirty();
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
		cmd.setPerClientAuthentication(true);
		
		addStartupProcessor(cmd);
		
		LoginManager mgr = getMessageBroker().getLoginManager();
		assertTrue("LoginManager not started",mgr.isStarted());
		assertSame("SpringSecurityLoginCommand not set on the LoginManager",cmd, mgr.getLoginCommand());
		assertTrue("Should be set to per client authentication", mgr.isPerClientAuthentication());
		
	}
	
	public void testDoAuthentication_ValidLogin() throws Exception {
		String username = "foo";
		String password = "bar";
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
		
		Principal principal = cmd.doAuthentication(username, password);
		
		assertNotNull("A non-null Principal was not returned", principal);
		assertEquals(username, principal.getName());
	}
	
	public void testDoAuthentication_Failure() throws Exception {
		
		String username = "foo";
		String password = "bar";
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(false));
		
		try {
			cmd.doAuthentication(username, password);
			fail("An AuthenticationException was not thrown");
		} catch (AuthenticationException ex) {
			//expected
		}
	}
	
	public void testDoAuthorization_MatchingAuthority() throws Exception {
		GrantedAuthority[] authorities = new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER"), new GrantedAuthorityImpl("ROLE_ABUSER")};
		Principal principal = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
		
		List<String> roles = new ArrayList<String>();
		roles.add("ROLE_ADMIN");
		roles.add("ROLE_USER");
		assertTrue("Authorization should pass", cmd.doAuthorization(principal, roles));
	}
	
	public void testDoAuthorization_NoMatchingAuthority() throws Exception {
		GrantedAuthority[] authorities = new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER"), new GrantedAuthorityImpl("ROLE_ABUSER")};
		Principal principal = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
		
		List<String> roles = new ArrayList<String>();
		roles.add("ROLE_ADMIN");
		assertFalse("Authorization should pass", cmd.doAuthorization(principal, roles));
	}
	
	public void testLogoutWithDefaults() throws Exception {
		String username = "foo";
		String password = "bar";

		MockFlexSession session = new MockFlexSession();
		FlexContext.setThreadLocalSession(session);
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
		
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
		
		cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
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
