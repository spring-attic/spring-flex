/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.core;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;

import flex.messaging.MessageException;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;

public class MessageInterceptionAdviceTests {

    @Mock
    private AbstractEndpoint endpoint;

    @Mock
    private Message inMessage;

    @Mock
    private Message outMessage;

    @Mock
    private Message mutatedInMessage;

    @Mock
    private Message mutatedOutMessage;

    private AbstractEndpoint advisedEndpoint;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void exceptionPassthrough() {
        PassthroughInterceptor interceptor = new PassthroughInterceptor();
        setupInterceptor(interceptor);
        MessageException error = new MessageException();
        when(this.endpoint.serviceMessage(this.inMessage)).thenThrow(error);

        Exception result = null;
        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
        } catch (MessageException ex) {
            result = ex;
        }
        assertSame(error, result);
        assertTrue(interceptor.preInvoked);
        assertFalse(interceptor.postInvoked);
    }

    @Test
    public void messageMutatingInterceptor() {

        MessageMutatingInterceptor interceptor = new MessageMutatingInterceptor();
        setupInterceptor(interceptor);
        when(this.endpoint.serviceMessage(this.mutatedInMessage)).thenReturn(this.outMessage);

        Message result = this.advisedEndpoint.serviceMessage(this.inMessage);

        assertNotNull(result);
        assertSame(this.mutatedOutMessage, result);
        assertTrue(interceptor.preInvoked);
        assertTrue(interceptor.postInvoked);

    }

    @Test
    public void passthroughInterceptor() {

        PassthroughInterceptor interceptor = new PassthroughInterceptor();
        setupInterceptor(interceptor);
        when(this.endpoint.serviceMessage(this.inMessage)).thenReturn(this.outMessage);

        Message result = this.advisedEndpoint.serviceMessage(this.inMessage);

        assertSame(this.outMessage, result);
        assertTrue(interceptor.preInvoked);
        assertTrue(interceptor.postInvoked);

    }

    @Test
    public void resourceHandlingInterceptorOnPreprocessException() {
        PreProcessExceptionInterceptor interceptor = new PreProcessExceptionInterceptor();
        setupInterceptor(interceptor);
        TestException result = null;
        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
        } catch(TestException ex) {
            result = ex;
        }
        assertNotNull("Exception not re-thrown", result);
        assertTrue("Interceptor not completed", interceptor.completed);
    }

    @Test
    public void resourceHandlingInterceptorOnInvocationException() {
        ResourceHandlingInterceptor interceptor = new ResourceHandlingInterceptor();
        setupInterceptor(interceptor);
        when(this.endpoint.serviceMessage(this.inMessage)).thenThrow(new TestException());
        TestException result = null;
        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
        } catch(TestException ex) {
            result = ex;        
        }
        assertNotNull("Exception not re-thrown", result);
        assertTrue("Interceptor not completed", interceptor.completed);
    }

    @Test
    public void testResourceHandlingInterceptorOnPostprocessException() {
        PostProcessExceptionInterceptor interceptor = new PostProcessExceptionInterceptor();
        setupInterceptor(interceptor);
        when(this.endpoint.serviceMessage(this.inMessage)).thenThrow(new TestException());
        TestException result = null;
        try {
            this.advisedEndpoint.serviceMessage(this.inMessage);
        } catch(TestException ex) {
            result = ex;        
        }
        assertNotNull("Exception not re-thrown", result);
        assertNotNull("Preprocess not called", interceptor.preInvoked);
        assertTrue("Interceptor not completed", interceptor.completed);
    }

    @Test
    public void resourceHandlingInterceptorOnSuccess() {
        ResourceHandlingInterceptor interceptor = new ResourceHandlingInterceptor();
        setupInterceptor(interceptor);
        when(this.endpoint.serviceMessage(this.inMessage)).thenReturn(this.outMessage);
        
        Message result = this.advisedEndpoint.serviceMessage(this.inMessage);
        
        assertSame(this.outMessage, result);
        assertTrue("Interceptor not completed", interceptor.completed);
    }

    private void setupInterceptor(MessageInterceptor interceptor) {
        ProxyFactory factory = new ProxyFactory();
        factory.setProxyTargetClass(true);
        MessageInterceptionAdvice advice = new MessageInterceptionAdvice();
        advice.getMessageInterceptors().add(interceptor);
        factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(advice));
        factory.setTarget(this.endpoint);
        this.advisedEndpoint = (AbstractEndpoint) factory.getProxy();
    }

    public class MessageMutatingInterceptor implements MessageInterceptor {

        protected boolean preInvoked = false;

        protected boolean postInvoked = false;

        public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
            this.postInvoked = true;
            return MessageInterceptionAdviceTests.this.mutatedOutMessage;
        }

        public Message preProcess(MessageProcessingContext context, Message inputMessage) {
            this.preInvoked = true;
            return MessageInterceptionAdviceTests.this.mutatedInMessage;
        }
    }

    public class PassthroughInterceptor implements MessageInterceptor {

        protected boolean preInvoked = false;

        protected boolean postInvoked = false;

        public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
            this.postInvoked = true;
            return outputMessage;
        }

        public Message preProcess(MessageProcessingContext context, Message inputMessage) {
            this.preInvoked = true;
            return inputMessage;
        }
    }
    
    public class ResourceHandlingInterceptor implements ResourceHandlingMessageInterceptor {

        protected boolean completed = false;
        
        public void afterCompletion(MessageProcessingContext context, Message inputMessage, Message outputMessage, Exception ex) {
            completed = true;
        }

        public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
            return outputMessage;
        }

        public Message preProcess(MessageProcessingContext context, Message inputMessage) {
            return inputMessage;
        }        
    }
    
    public class PreProcessExceptionInterceptor implements ResourceHandlingMessageInterceptor {
        protected boolean completed = false;
        
        public void afterCompletion(MessageProcessingContext context, Message inputMessage, Message outputMessage, Exception ex) {
            completed = true;
        }

        public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
            return outputMessage;
        }

        public Message preProcess(MessageProcessingContext context, Message inputMessage) {
            throw new TestException();
        }       
    }
    
    public class PostProcessExceptionInterceptor implements ResourceHandlingMessageInterceptor {
        protected boolean completed = false;
        
        protected boolean preInvoked = false;
        
        public void afterCompletion(MessageProcessingContext context, Message inputMessage, Message outputMessage, Exception ex) {
            completed = true;
        }

        public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
            throw new TestException();
        }

        public Message preProcess(MessageProcessingContext context, Message inputMessage) {
            preInvoked = true;
            return inputMessage;
        }       
    }
    
    @SuppressWarnings("serial")
    public static class TestException extends RuntimeException {
        
    }

}
