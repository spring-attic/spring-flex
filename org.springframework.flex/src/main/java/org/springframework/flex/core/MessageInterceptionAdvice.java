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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import flex.messaging.messages.Message;

/**
 * AOP interceptor that applies any provided {@link MessageInterceptor}s to the AMF {@link Message} being processed.
 * 
 * @author Jeremy Grelle
 */
public class MessageInterceptionAdvice implements MethodInterceptor {

    private Set<MessageInterceptor> messageInterceptors = new HashSet<MessageInterceptor>();

    /**
     * Returns the chain of provided {@link MessageInterceptor}s
     * 
     * @return the message interceptors
     */
    public Set<MessageInterceptor> getMessageInterceptors() {
        return this.messageInterceptors;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Object invoke(MethodInvocation mi) throws Throwable {
        MessageProcessingContext context = new MessageProcessingContext(mi.getThis());
        Message inputMessage = (Message) mi.getArguments()[0];
        for (MessageInterceptor interceptor : this.messageInterceptors) {
            inputMessage = interceptor.preProcess(context, inputMessage);
        }
        mi.getArguments()[0] = inputMessage;
        Message outputMessage = null;
        try {
            outputMessage = (Message) mi.proceed();
        } finally {
            if (outputMessage != null) {
                Stack<MessageInterceptor> postProcessStack = new Stack<MessageInterceptor>();
                postProcessStack.addAll(this.messageInterceptors);
                while (!postProcessStack.empty()) {
                    MessageInterceptor interceptor = postProcessStack.pop();
                    outputMessage = interceptor.postProcess(context, inputMessage, outputMessage);
                }
            }
        }
        return outputMessage;
    }

    /**
     * Sets the chain of provided {@link MessageInterceptor}s
     * 
     * @param messageInterceptors the message interceptors
     */
    public void setMessageInterceptors(Set<MessageInterceptor> messageInterceptors) {
        this.messageInterceptors = messageInterceptors;
    }

}
