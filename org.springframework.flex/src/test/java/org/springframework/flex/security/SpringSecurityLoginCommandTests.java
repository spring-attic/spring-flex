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
import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.core.AbstractMessageBrokerTests;
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

    public void testDoAuthentication_Failure() throws Exception {

        String username = "foo";
        String password = "bar";

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(false));

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

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));

        Principal principal = this.cmd.doAuthentication(username, password);

        assertNotNull("A non-null Principal was not returned", principal);
        assertEquals(username, principal.getName());
    }

    public void testDoAuthorization_MatchingAuthority() throws Exception {
        GrantedAuthority[] authorities = new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_USER"), new GrantedAuthorityImpl("ROLE_ABUSER") };
        Principal principal = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));

        List<String> roles = new ArrayList<String>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        assertTrue("Authorization should pass", this.cmd.doAuthorization(principal, roles));
    }

    public void testDoAuthorization_NoMatchingAuthority() throws Exception {
        GrantedAuthority[] authorities = new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_USER"), new GrantedAuthorityImpl("ROLE_ABUSER") };
        Principal principal = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));

        List<String> roles = new ArrayList<String>();
        roles.add("ROLE_ADMIN");
        assertFalse("Authorization should pass", this.cmd.doAuthorization(principal, roles));
    }

    public void testLoginCommandRegisteredWithDefaultConfig() throws Exception {

        setDirty();

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));

        addStartupProcessor(this.cmd);

        LoginManager mgr = getMessageBroker().getLoginManager();
        assertTrue("LoginManager not started", mgr.isStarted());
        assertSame("SpringSecurityLoginCommand not set on the LoginManager", this.cmd, mgr.getLoginCommand());
        assertFalse("Should default to per session authentication", mgr.isPerClientAuthentication());

    }

    public void testLoginCommandRegisteredWithPerClientConfig() throws Exception {

        setDirty();

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));
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

        this.cmd = new SpringSecurityLoginCommand(new MockAuthenticationManager(true));

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
