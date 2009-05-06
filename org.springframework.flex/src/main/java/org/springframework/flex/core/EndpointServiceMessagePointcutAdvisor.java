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

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.util.ReflectionUtils;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;
import flex.messaging.services.Service;

/**
 * Static method-matching pointcut advisor that applies security advice to invocations of
 * {@link AbstractEndpoint#serviceMessage(Message)}.
 * 
 * <p>
 * This is the critical point where incoming AMF messages have been deserialized and are ready to be routed to a BlazeDS
 * {@link Service}. Iterception at this point in the deserialization/serialization process is needed to be able to
 * easily send proper AMF error messages back to the client.
 * 
 * @author Jeremy Grelle
 */
@SuppressWarnings( { "unchecked", "serial" })
public class EndpointServiceMessagePointcutAdvisor extends StaticMethodMatcherPointcutAdvisor implements EndpointAdvisor {

    private static final String SERVICE_MESSAGE_METHOD_NAME = "serviceMessage";

    private final Class[] SERVICE_MESSAGE_ARGS = new Class[] { Message.class };

    public EndpointServiceMessagePointcutAdvisor(Advice advice) {
        super(advice);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean matches(Method m, Class c) {
        return AbstractEndpoint.class.isAssignableFrom(c)
            && m.equals(ReflectionUtils.findMethod(AbstractEndpoint.class, SERVICE_MESSAGE_METHOD_NAME, this.SERVICE_MESSAGE_ARGS));
    }
}
