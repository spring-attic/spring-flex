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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.RequestMatcher;

import flex.messaging.FlexContext;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class EndpointInterceptorTests extends TestCase {

    @Mock
    private AuthenticationManager mgr;
    
    @Mock
    private AbstractEndpoint endpoint;

    @Mock
    private Message inMessage;

    @Mock
    private Message outMessage;
    
    @Mock
    private MockHttpServletRequest request;

    private AbstractEndpoint advisedEndpoint;

    @Override
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);

        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<ConfigAttribute> attrs = new ArrayList<ConfigAttribute>();
        attrs.add(new SecurityConfig("ROLE_USER"));
        requestMap.put(new AntPathRequestMatcher("/messagebroker/amf"), attrs);
        EndpointSecurityMetadataSource source = new EndpointSecurityMetadataSource(requestMap);

        EndpointInterceptor interceptor;
        interceptor = new EndpointInterceptor();
        interceptor.setAuthenticationManager(this.mgr);
        interceptor.setObjectDefinitionSource(source);
        interceptor.afterPropertiesSet();
        MessageInterceptionAdvice advice = new MessageInterceptionAdvice();
        advice.getMessageInterceptors().add(interceptor);

        ProxyFactory factory = new ProxyFactory();
        factory.setProxyTargetClass(true);
        factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(advice));
        factory.setTarget(this.endpoint);
        this.advisedEndpoint = (AbstractEndpoint) factory.getProxy();
        
        this.request = new MockHttpServletRequest();
        FlexContext.setThreadLocalHttpRequest(this.request);
    }

    @Override
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
        FlexContext.clearThreadLocalObjects();
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

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Message result = this.advisedEndpoint.serviceMessage(this.inMessage);

        assertSame(this.outMessage, result);
    }

    public void testServiceUnauthenticated() throws Exception {

        this.request.setServletPath("/messagebroker");
        this.request.setPathInfo("/amf");
        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
            fail("An AuthenticationException should be thrown");
        } catch (AuthenticationException ex) {
            // expected
        }
    }

    public void testServiceUnauthorized() throws Exception {

        this.request.setServletPath("/messagebroker");
        this.request.setPathInfo("/amf");

        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", new ArrayList<GrantedAuthority>());
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
