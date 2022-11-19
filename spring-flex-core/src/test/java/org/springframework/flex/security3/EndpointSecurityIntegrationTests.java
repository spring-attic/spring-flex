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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.support.AopUtils;
import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.flex.core.EndpointAdvisor;
import org.springframework.flex.core.EndpointConfigProcessor;
import org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor;
import org.springframework.flex.core.ExceptionTranslationAdvice;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.messages.Message;
import flex.messaging.security.SecurityException;

public class EndpointSecurityIntegrationTests extends AbstractMessageBrokerTests {

    private EndpointSecurityMetadataSource source;

    private AccessDecisionManager adm;

    @Mock
    private AuthenticationManager mgr;
    
    @Mock
    private Message message;
    
    @Mock
    private MockHttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        List<ConfigAttribute> attrs = new ArrayList<ConfigAttribute>();
        attrs.add(new SecurityConfig("ROLE_USER"));
        requestMap.put(new AntPathRequestMatcher("/messagebroker/**"), attrs);
        this.source = new EndpointSecurityMetadataSource(requestMap);

        List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
        voters.add(new RoleVoter());
        this.adm = new AffirmativeBased(voters);
        
        initializeInterceptors();
        
        this.request = new MockHttpServletRequest();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
        FlexContext.clearThreadLocalObjects();
    }

    @Test
    public void serviceAuthorized() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MessageBroker broker = getMessageBroker();
        Endpoint endpoint = broker.getEndpoint("my-amf");
        assertNotNull(endpoint);

        try {
            ((AbstractEndpoint) endpoint).serviceMessage(this.message);
            fail("An exception should be thrown since we're using a mock message");
        } catch (MessageException ex) {
            assertFalse(ex instanceof SecurityException);
        }
    }

    @Test
    public void serviceUnauthenticated() throws Exception {
        MessageBroker broker = getMessageBroker();
        Endpoint endpoint = broker.getEndpoint("my-amf");
        assertNotNull(endpoint);

        FlexContext.setThreadLocalHttpRequest(this.request);
        this.request.setServletPath("/messagebroker");
        this.request.setPathInfo("/amf");
        
        try {
            ((AbstractEndpoint) endpoint).serviceMessage(this.message);
            fail("A SecurityException should be thrown");
        } catch (SecurityException ex) {
            assertTrue(ex.getCode().equals(SecurityException.CLIENT_AUTHENTICATION_CODE));
            assertTrue(ex.getRootCause() instanceof AuthenticationException);
        } catch (MessageException ex) {
            fail("A SecurityException should be thrown");
        }

    }

    @Test
    public void serviceUnauthorized() throws Exception {

        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", new ArrayList<GrantedAuthority>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        MessageBroker broker = getMessageBroker();
        Endpoint endpoint = broker.getEndpoint("my-amf");
        assertNotNull(endpoint);
        
        FlexContext.setThreadLocalHttpRequest(this.request);
        this.request.setServletPath("/messagebroker");
        this.request.setPathInfo("/amf");

        try {
            ((AbstractEndpoint) endpoint).serviceMessage(this.message);
            fail("A SecurityException should be thrown");
        } catch (SecurityException ex) {
            assertTrue(ex.getCode().equals(SecurityException.CLIENT_AUTHORIZATION_CODE));
            assertTrue(ex.getRootCause() instanceof AccessDeniedException);
        } catch (MessageException ex) {
            fail("A SecurityException should be thrown");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void startupProcessed() throws Exception {
        MessageBroker broker = getMessageBroker();
        Iterator i = broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Proxied endpoint1 must implement Endpoint", endpoint instanceof Endpoint);
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            assertTrue("Endpoint should be started", ((Endpoint) endpoint).isStarted());
        }
    }

    private void initializeInterceptors() throws Exception {
        setDirty();

        ExceptionTranslationAdvice translator = new ExceptionTranslationAdvice();
        translator.getExceptionTranslators().add(new SecurityExceptionTranslator());

        EndpointInterceptor endpointInterceptor = new EndpointInterceptor();
        endpointInterceptor.setAuthenticationManager(this.mgr);
        endpointInterceptor.setAccessDecisionManager(this.adm);
        endpointInterceptor.setObjectDefinitionSource(this.source);
        MessageInterceptionAdvice interceptor = new MessageInterceptionAdvice();
        interceptor.getMessageInterceptors().add(endpointInterceptor);

        List<EndpointAdvisor> advisors = new ArrayList<EndpointAdvisor>();
        advisors.add(new EndpointServiceMessagePointcutAdvisor(translator));
        advisors.add(new EndpointServiceMessagePointcutAdvisor(interceptor));

        EndpointConfigProcessor processor = new EndpointConfigProcessor(advisors);

        addStartupProcessor(processor);
    }

    public class MyEndpoint extends AMFEndpoint {

    }

}
