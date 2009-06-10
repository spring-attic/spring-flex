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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.support.AopUtils;
import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.flex.core.EndpointAdvisor;
import org.springframework.flex.core.EndpointConfigProcessor;
import org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor;
import org.springframework.flex.core.ExceptionTranslationAdvice;
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

import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.messages.Message;
import flex.messaging.security.SecurityException;

public class EndpointSecurityIntegrationTests extends AbstractMessageBrokerTests {

    private final MockAuthenticationManager mgr = new MockAuthenticationManager();

    private EndpointDefinitionSource source;

    private final AccessDecisionManager adm = new AffirmativeBased();

    @Mock
    private Message message;

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        LinkedHashMap requestMap = new LinkedHashMap();
        requestMap.put(new RequestKey("**/messagebroker/**"), new ConfigAttributeDefinition("ROLE_USER"));
        this.source = new EndpointDefinitionSource(new AntUrlPathMatcher(), requestMap);

        List voters = new ArrayList();
        voters.add(new RoleVoter());
        ((AffirmativeBased) this.adm).setDecisionVoters(voters);

        initializeInterceptors();
    }

    @Override
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void testServiceAuthorized() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_USER") });
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

    public void testServiceUnauthenticated() throws Exception {
        MessageBroker broker = getMessageBroker();
        Endpoint endpoint = broker.getEndpoint("my-amf");
        assertNotNull(endpoint);

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

    public void testServiceUnauthorized() throws Exception {

        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", new GrantedAuthority[] {});
        SecurityContextHolder.getContext().setAuthentication(auth);

        MessageBroker broker = getMessageBroker();
        Endpoint endpoint = broker.getEndpoint("my-amf");
        assertNotNull(endpoint);

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

    @SuppressWarnings("unchecked")
    public void testStartupProcessed() throws Exception {
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
