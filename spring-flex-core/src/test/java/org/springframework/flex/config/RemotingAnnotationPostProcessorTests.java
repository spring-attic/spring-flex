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

package org.springframework.flex.config;

import java.util.Arrays;
import java.util.Iterator;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;
import static org.junit.Assert.*;
import org.junit.Test;

@ContextConfiguration(locations="classpath:org/springframework/flex/config/remote-service-annotations.xml", loader=RemotingAnnotationPostProcessorTests.ParentContextLoader.class)
public class RemotingAnnotationPostProcessorTests extends AbstractFlexConfigurationTests {

    private MessageBroker broker;

    @Test
    public void exportAnnotatedBeanWithAutowiredConstructor() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedAutowiredRemoteBean");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", AnnotatedAutowiredRemoteBean.class.getName(), rd.getSource());
    }

    @Test
    public void exportAnnotatedScopedProxyBeanWithAutowiredConstructor() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedAutowiredScopedProxyRemoteBean");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", AnnotatedAutowiredScopedProxyRemoteBean.class.getName(), rd.getSource());
    }

    @Test
    public void exportAnnotatedBeanWithCustomChannelsAndPostConstruct() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedRemoteBean");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", AnnotatedRemoteBean.class.getName(), rd.getSource());
        assertTrue("Channels not set", rd.getChannels().contains("my-amf") && rd.getChannels().contains("my-secure-amf"));
        AnnotatedRemoteBean bean = applicationContext.getBean(AnnotatedRemoteBean.class);
        assertTrue("@PostConstruct method not invoked", bean.initInvoked);
    }

    @Test
    public void exportAnnotatedBeanFromParentContextWithDefaults() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedRemoteBean3");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", MyService1.class.getName(), rd.getSource());
    }

    @Test
    public void exportAnnotatedXmlConfiguredBeanWithDefaults() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedRemoteBean1");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", MyService1.class.getName(), rd.getSource());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void exportBeanWithCustomSettings() {
        this.broker = (MessageBroker) applicationContext.getBean("annotatedRemoteServiceBroker", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("exportedAnnotatedRemoteBean2");
        assertNotNull("Destination not found", rd);
        String[] channels = new String[] { "my-amf", "my-secure-amf" };
        assertEquals("Channels not set", Arrays.asList(channels), rd.getChannels());

        assertTrue("Custom adapter not set", rd.getAdapter() instanceof TestAdapter);

        String[] includeNames = new String[] { "foo", "bar" };
        String[] excludeNames = new String[] { "zoo", "baz" };

        assertTrue("No included methods found", ((JavaAdapter) rd.getAdapter()).getIncludeMethodIterator().hasNext());
        Iterator includes = ((JavaAdapter) rd.getAdapter()).getIncludeMethodIterator();
        while (includes.hasNext()) {
            RemotingMethod include = (RemotingMethod) includes.next();
            assertTrue(Arrays.asList(includeNames).contains(include.getName()));
            assertFalse(Arrays.asList(excludeNames).contains(include.getName()));
        }

        assertTrue("No excluded methods found", ((JavaAdapter) rd.getAdapter()).getExcludeMethodIterator().hasNext());
        Iterator excludes = ((JavaAdapter) rd.getAdapter()).getExcludeMethodIterator();
        while (includes.hasNext()) {
            RemotingMethod exclude = (RemotingMethod) excludes.next();
            assertTrue(Arrays.asList(excludeNames).contains(exclude.getName()));
            assertFalse(Arrays.asList(includeNames).contains(exclude.getName()));
        }
    }

    @Test
    public void exportBeanWithDynamicChannels() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedDynamicRemoteBean1");
        assertNotNull("Destination not found", rd);
        String[] channels = new String[] {"my-amf"};
        assertEquals("Channels not set", Arrays.asList(channels), rd.getChannels());
    }

    @Test
    public void exportBeanWithMultipleDynamicChannels() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedDynamicRemoteBean2");
        assertNotNull("Destination not found", rd);
        String[] channels = new String[] {"my-secure-amf", "my-amf"};
        assertEquals("Channels not set", Arrays.asList(channels), rd.getChannels());
    }

    @Test
    public void exportBeanWithMultipleDynamicCombinedChannels() {
        this.broker = (MessageBroker) applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedDynamicRemoteBean3");
        assertNotNull("Destination not found", rd);
        String[] channels = new String[] {"my-amf", "my-secure-amf"};
        assertEquals("Channels not set", Arrays.asList(channels), rd.getChannels());
    }

    public static class MyDependency {
    }

    @RemotingDestination
    public static class MyService1 {
    }

    @RemotingDestination(value = "exportedAnnotatedRemoteBean2", messageBroker = "annotatedRemoteServiceBroker", channels = { "my-amf", "my-secure-amf" }, serviceAdapter = "customAdapter1")
    public static class MyService2 {

        @RemotingInclude
        public void bar() {
        }

        @RemotingExclude
        public void baz() {
        }

        @RemotingInclude
        public void foo() {
        }

        @RemotingExclude
        public void zoo() {
        }
    }
    
    @RemotingDestination(channels="${com.foo.bar}")
    public static class MyService3 {
    }
    
    @RemotingDestination(channels="${com.foo.baz}")
    public static class MyService4 {
    }
    
    @RemotingDestination(channels={"${com.foo.bar}", "${com.foo.baz}"})
    public static class MyService5 {
    }
    
    public static final class TestAdapter extends JavaAdapter {
    }
    
    public static class ParentContextLoader extends MessageBrokerContextLoader {
        
        @Override
        protected ConfigurableApplicationContext createParentContext() {
            GenericWebApplicationContext context = new GenericWebApplicationContext();
            context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
            new XmlBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/config/annotated-remote-bean-context.xml" });
            context.refresh();
            return context;
        }
    }
}
