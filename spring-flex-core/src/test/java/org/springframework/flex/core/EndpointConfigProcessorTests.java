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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.Advised;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;

public class EndpointConfigProcessorTests extends TestCase {

    MessageBroker broker;

    @Mock
    AbstractEndpoint endpoint1;

    @Mock
    AbstractEndpoint endpoint2;
    
    CustomEndpoint endpoint3;

    @Mock
    MethodInterceptor advice1;

    @Mock
    MethodInterceptor advice2;
    
    @Mock
    Message testMessage;

    EndpointServiceMessagePointcutAdvisor advisor1;

    EndpointServiceMessagePointcutAdvisor advisor2;

    EndpointConfigProcessor processor;

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.advisor1 = new EndpointServiceMessagePointcutAdvisor(this.advice1);
        this.advisor2 = new EndpointServiceMessagePointcutAdvisor(this.advice2);

        this.broker = new MessageBroker();

        when(this.endpoint1.getId()).thenReturn("bar");
        when(this.endpoint1.getUrl()).thenReturn("http://foo.com/bar");
        when(this.endpoint1.serviceMessage(this.testMessage)).thenReturn(this.testMessage);
        this.broker.addEndpoint(this.endpoint1);

        when(this.endpoint2.getId()).thenReturn("baz");
        when(this.endpoint2.getUrl()).thenReturn("http://foo.com/baz");
        when(this.endpoint2.serviceMessage(this.testMessage)).thenReturn(this.testMessage);
        this.broker.addEndpoint(this.endpoint2);
        
        this.endpoint3 = new CustomEndpoint();
        this.endpoint3.setId("custom");
        this.endpoint3.setUrl("http://foo.com/custom");
        this.broker.addEndpoint(this.endpoint3);
    }

    @SuppressWarnings("unchecked")
    public void testPostProcessAfterInit() throws Throwable {

        List<EndpointAdvisor> advisors = new ArrayList<EndpointAdvisor>();
        advisors.add(this.advisor1);
        advisors.add(this.advisor2);
        this.processor = new EndpointConfigProcessor(advisors);

        MessageBroker processedBroker = this.processor.processAfterStartup(this.broker);
        assertSame(this.broker, processedBroker);

        Collection endpoints = processedBroker.getEndpoints().values();

        int counter = 0;
        Iterator i = endpoints.iterator();
        while (i.hasNext()) {
            AbstractEndpoint endpoint = (AbstractEndpoint) i.next();

            assertTrue(endpoint instanceof Advised);

            Advised advisedEndpoint = (Advised) endpoint;

            assertTrue(advisedEndpoint.getAdvisors().length == 2);
            assertTrue(advisedEndpoint.isFrozen());
            assertTrue(advisedEndpoint.isProxyTargetClass());
            assertTrue(advisedEndpoint.indexOf(this.advisor1) == 0);
            assertTrue(advisedEndpoint.indexOf(this.advisor2) == 1);
            
            counter++;
            
            endpoint.serviceMessage(this.testMessage);
            
            ArgumentCaptor<MethodInvocation> arg = ArgumentCaptor.forClass(MethodInvocation.class);
            verify(advice1, times(counter)).invoke(arg.capture());
            arg.getValue().proceed();
            verify(advice2, times(counter)).invoke(arg.capture());
            Object result = arg.getValue().proceed();
            
            assertEquals(this.testMessage, result);
        }
    }
    
    public static class CustomEndpoint extends AMFEndpoint {

        @Override
        public Message serviceMessage(Message message) {
            return message;
        }
    }

}
