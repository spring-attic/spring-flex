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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.amf.AMFFilter;

/**
 * Processor that applies advice to configured BlazeDS endpoints by wrapping them in Spring AOP proxies.
 * 
 * <p>
 * This processor will be automatically configured through the <code>message-broker</code> xml configuration namespace
 * tag.
 * 
 * @author Jeremy Grelle
 */
public class EndpointConfigProcessor implements MessageBrokerConfigProcessor, BeanClassLoaderAware {

    private final EndpointAdvisor[] advisors;

    private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

    public EndpointConfigProcessor(List<EndpointAdvisor> advisors) {
        Assert.notEmpty(advisors, "A non-empty list of EndpointServiceMessagePointcutAdvisors is required");
        this.advisors = advisors.toArray(new EndpointAdvisor[advisors.size()]);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public MessageBroker processAfterStartup(MessageBroker broker) {
        Iterator i = broker.getEndpoints().keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            Object endpoint = broker.getEndpoints().get(key);
            ProxyFactory factory = new ProxyFactory();
            factory.setProxyTargetClass(true);
            factory.addAllAdvisors(this.advisors);
            factory.setTarget(endpoint);
            factory.setFrozen(true);
            Object proxy = factory.getProxy(this.proxyClassLoader);
            fixFilterChain(endpoint, proxy);
            broker.getEndpoints().put(key, proxy);
        }
        return broker;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public MessageBroker processBeforeStartup(MessageBroker broker) {
        return broker;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.proxyClassLoader = classLoader;
    }

    private void fixFilterChain(Object endpoint, Object proxy) {
        // This is a nasty workaround, required because the advised Endpoint
        // passes a reference to itself to some of the filters in the chain.
        // It would be nice if the endpoints actually exposed their filter chain for
        // easier modification.
        Field filterChainField = ReflectionUtils.findField(endpoint.getClass(), "filterChain");
        if (filterChainField != null) {
            Assert.isAssignable(AMFFilter.class, filterChainField.getType(), "filterChain field is expected to be of type AMFFilter");
            ReflectionUtils.makeAccessible(filterChainField);
            AMFFilter filter = (AMFFilter) ReflectionUtils.getField(filterChainField, endpoint);
            while (filter != null) {
                Field endpointField = ReflectionUtils.findField(filter.getClass(), "endpoint");
                if (endpointField != null) {
                    ReflectionUtils.makeAccessible(endpointField);
                    ReflectionUtils.setField(endpointField, filter, proxy);
                }
                filter = filter.getNext();
            }

        }
    }
}
