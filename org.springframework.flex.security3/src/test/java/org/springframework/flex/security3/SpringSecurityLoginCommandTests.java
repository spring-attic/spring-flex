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

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.security.LoginManager;

public class SpringSecurityLoginCommandTests extends AbstractMessageBrokerTests {

    @Mock
    AuthenticationManager mgr;
    
    SpringSecurityLoginCommand cmd;
    
    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testDoAuthentication_Failure() throws Exception {

        String username = "foo";
        String password = "bar";

        when(mgr.authenticate(isA(Authentication.class))).thenThrow(new UsernameNotFoundException("Authentication failed"));
        this.cmd = new SpringSecurityLoginCommand(mgr);

        try {
            this.cmd.doAuthentication(username, password);
            fail("An AuthenticationException was not thrown");
        } catch (AuthenticationException ex) {
            // expected
        }
    }

    public void testDoAuthentication_ValidLogin() throws Exception {
        String username = "foo";
        String password = "bar";

        when(mgr.authenticate(isA(Authentication.class))).thenReturn(new UsernamePasswordAuthenticationToken(username, password));
        this.cmd = new SpringSecurityLoginCommand(mgr);

        Principal principal = this.cmd.doAuthentication(username, password);

        assertNotNull("A non-null Principal was not returned", principal);
        assertEquals(username, principal.getName());
    }

    public void testDoAuthorization_MatchingAuthority() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        authorities.add(new GrantedAuthorityImpl("ROLE_ABUSER"));
        Principal principal = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);

        this.cmd = new SpringSecurityLoginCommand(mgr);

        List<String> roles = new ArrayList<String>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        assertTrue("Authorization should pass", this.cmd.doAuthorization(principal, roles));
    }

    public void testDoAuthorization_NoMatchingAuthority() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        authorities.add(new GrantedAuthorityImpl("ROLE_ABUSER"));
        Principal principal = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);

        this.cmd = new SpringSecurityLoginCommand(mgr);

        List<String> roles = new ArrayList<String>();
        roles.add("ROLE_ADMIN");
        assertFalse("Authorization should not pass", this.cmd.doAuthorization(principal, roles));
    }

    public void testLoginCommandRegisteredWithDefaultConfig() throws Exception {

        setDirty();

        this.cmd = new SpringSecurityLoginCommand(mgr);

        addStartupProcessor(this.cmd);

        LoginManager mgr = getMessageBroker().getLoginManager();
        assertTrue("LoginManager not started", mgr.isStarted());
        assertSame("SpringSecurityLoginCommand not set on the LoginManager", this.cmd, mgr.getLoginCommand());
        assertFalse("Should default to per session authentication", mgr.isPerClientAuthentication());

    }

    public void testLoginCommandRegisteredWithPerClientConfig() throws Exception {

        setDirty();

        this.cmd = new SpringSecurityLoginCommand(mgr);
        this.cmd.setPerClientAuthentication(true);

        addStartupProcessor(this.cmd);

        LoginManager mgr = getMessageBroker().getLoginManager();
        assertTrue("LoginManager not started", mgr.isStarted());
        assertSame("SpringSecurityLoginCommand not set on the LoginManager", this.cmd, mgr.getLoginCommand());
        assertTrue("Should be set to per client authentication", mgr.isPerClientAuthentication());

    }

    public void testLogoutWithDefaults() throws Exception {
        String username = "foo";
        String password = "bar";

        MockFlexSession session = new MockFlexSession();
        FlexContext.setThreadLocalSession(session);

        this.cmd = new SpringSecurityLoginCommand(mgr);

        Principal principal = this.cmd.doAuthentication(username, password);

        SecurityContext original = SecurityContextHolder.getContext();

        this.cmd.logout(principal);

        assertTrue("SecurityContext was not cleared", original != SecurityContextHolder.getContext());
    }

    private static class MockFlexSession extends FlexSession {

        private boolean valid = true;

        @Override
        public String getId() {
            return "mockFlexSession";
        }

        @Override
        public void invalidate() {
            this.valid = false;
        }

        @Override
        public boolean isPushSupported() {
            return false;
        }

        @Override
        public boolean isValid() {
            return this.valid;
        }
    }

}
