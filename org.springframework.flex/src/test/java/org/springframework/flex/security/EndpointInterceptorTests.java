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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.security.AccessDecisionManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.MockAuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.util.AntUrlPathMatcher;
import org.springframework.security.vote.AffirmativeBased;
import org.springframework.security.vote.RoleVoter;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class EndpointInterceptorTests extends TestCase {

    private final MockAuthenticationManager mgr = new MockAuthenticationManager();

    private final AccessDecisionManager adm = new AffirmativeBased();

    @Mock
    private AbstractEndpoint endpoint;

    @Mock
    private Message inMessage;

    @Mock
    private Message outMessage;

    private AbstractEndpoint advisedEndpoint;

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        LinkedHashMap requestMap = new LinkedHashMap();
        requestMap.put(new RequestKey("**/messagebroker/amf"), new ConfigAttributeDefinition("ROLE_USER"));
        EndpointDefinitionSource source = new EndpointDefinitionSource(new AntUrlPathMatcher(), requestMap);

        List voters = new ArrayList();
        voters.add(new RoleVoter());
        ((AffirmativeBased) this.adm).setDecisionVoters(voters);

        EndpointInterceptor interceptor;
        interceptor = new EndpointInterceptor();
        interceptor.setAuthenticationManager(this.mgr);
        interceptor.setAccessDecisionManager(this.adm);
        interceptor.setObjectDefinitionSource(source);
        MessageInterceptionAdvice advice = new MessageInterceptionAdvice();
        advice.getMessageInterceptors().add(interceptor);

        ProxyFactory factory = new ProxyFactory();
        factory.setProxyTargetClass(true);
        factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(advice));
        factory.setTarget(this.endpoint);
        this.advisedEndpoint = (AbstractEndpoint) factory.getProxy();
    }

    @Override
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void testLoginCommand() throws Exception {
        CommandMessage loginMessage = new CommandMessage(CommandMessage.LOGIN_OPERATION);
        when(this.endpoint.serviceMessage(loginMessage)).thenReturn(this.outMessage);

        Message result = this.advisedEndpoint.serviceMessage(loginMessage);

        assertSame(this.outMessage, result);

        verify(this.endpoint, never()).getUrlForClient();
    }

    public void testServiceAuthorized() throws Exception {
        when(this.endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amf");
        when(this.endpoint.serviceMessage(this.inMessage)).thenReturn(this.outMessage);

        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_USER") });
        SecurityContextHolder.getContext().setAuthentication(auth);

        Message result = this.advisedEndpoint.serviceMessage(this.inMessage);

        assertSame(this.outMessage, result);
    }

    public void testServiceUnauthenticated() throws Exception {

        when(this.endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amf");
        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
            fail("An AuthenticationException should be thrown");
        } catch (AuthenticationException ex) {
            // expected
        }
    }

    public void testServiceUnauthorized() throws Exception {

        when(this.endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amf");

        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", new GrantedAuthority[] {});
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
            fail("An AccessDeniedException should be thrown");
        } catch (AccessDeniedException ex) {
            // expected
        }
    }

    public void testServiceUnsecured() throws Exception {
        when(this.endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amfpolling");
        when(this.endpoint.serviceMessage(this.inMessage)).thenReturn(this.outMessage);

        Message result = this.advisedEndpoint.serviceMessage(this.inMessage);

        assertSame(this.outMessage, result);
    }

    public void testStart() {
        this.advisedEndpoint.start();
    }

}
