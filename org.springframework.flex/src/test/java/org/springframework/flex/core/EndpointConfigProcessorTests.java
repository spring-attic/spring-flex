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

package org.springframework.flex.core;

import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.Advised;
import org.springframework.util.ReflectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.endpoints.BaseHTTPEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.endpoints.amf.AMFFilter;

public class EndpointConfigProcessorTests extends TestCase {

    MessageBroker broker;

    @Mock
    AbstractEndpoint endpoint1;

    @Mock
    AbstractEndpoint endpoint2;

    @Mock
    MethodInterceptor advice1;

    @Mock
    MethodInterceptor advice2;

    EndpointServiceMessagePointcutAdvisor advisor1;

    EndpointServiceMessagePointcutAdvisor advisor2;

    EndpointConfigProcessor processor;

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.advisor1 = new EndpointServiceMessagePointcutAdvisor(this.advice1);
        this.advisor2 = new EndpointServiceMessagePointcutAdvisor(this.advice2);

        this.broker = new MessageBroker();
        Map endpoints = new HashMap();
        endpoints.put("foo", this.endpoint1);

        when(this.endpoint1.getId()).thenReturn("bar");
        when(this.endpoint1.getUrl()).thenReturn("http://foo.com/bar");
        this.broker.addEndpoint(this.endpoint1);

        when(this.endpoint2.getId()).thenReturn("baz");
        when(this.endpoint2.getUrl()).thenReturn("http://foo.com/baz");
        this.broker.addEndpoint(this.endpoint2);
    }

    @SuppressWarnings("unchecked")
    public void testPostProcessAfterInit() throws Exception {

        List<EndpointAdvisor> advisors = new ArrayList<EndpointAdvisor>();
        advisors.add(this.advisor1);
        advisors.add(this.advisor2);
        this.processor = new EndpointConfigProcessor(advisors);

        MessageBroker processedBroker = this.processor.processAfterStartup(this.broker);
        assertSame(this.broker, processedBroker);

        Collection endpoints = processedBroker.getEndpoints().values();

        Iterator i = endpoints.iterator();
        while (i.hasNext()) {
            Endpoint endpoint = (Endpoint) i.next();

            assertTrue(endpoint instanceof AbstractEndpoint);
            assertTrue(endpoint instanceof Advised);

            Advised advisedEndpoint = (Advised) endpoint;

            assertTrue(advisedEndpoint.getAdvisors().length == 2);
            assertTrue(advisedEndpoint.isFrozen());
            assertTrue(advisedEndpoint.isProxyTargetClass());
            assertTrue(advisedEndpoint.indexOf(this.advisor1) == 0);
            assertTrue(advisedEndpoint.indexOf(this.advisor2) == 1);

            Object targetEndpoint = advisedEndpoint.getTargetSource().getTarget();
            if (targetEndpoint instanceof BaseHTTPEndpoint) {
                Field filterChainField = ReflectionUtils.findField(targetEndpoint.getClass(), "filterChain");
                assertNotNull("Endpoint should have a filterChain field", filterChainField);
                AMFFilter filter = (AMFFilter) ReflectionUtils.getField(filterChainField, targetEndpoint);
                assertNotNull("Endpoint should have a populated filterChain field");
                while (filter != null) {
                    Field endpointField = ReflectionUtils.findField(filter.getClass(), "endpoint");
                    if (endpointField != null) {
                        Object endpointValue = ReflectionUtils.getField(endpointField, filter);
                        assertSame("AMFFilter's endpoint field should be the proxy instance", advisedEndpoint, endpointValue);
                    }
                    filter = filter.getNext();
                }
            }
        }
    }

}
