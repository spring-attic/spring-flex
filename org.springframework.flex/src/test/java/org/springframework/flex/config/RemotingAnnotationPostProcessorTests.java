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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.flex.config.xml.RemotingDestinationBeanDefinitionParserTests.TestAdapter;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class RemotingAnnotationPostProcessorTests extends AbstractFlexConfigurationTests {

    private MessageBroker broker;

    @Override
    protected ConfigurableApplicationContext createParentContext() {
        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
        createBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/config/annotated-remote-bean-context.xml" });
        context.refresh();
        return context;
    }

    public void testExportAnnotatedBeanWithAutowiredConstructor() {
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedAutowiredRemoteBean");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", AnnotatedAutowiredRemoteBean.class.getName(), rd.getSource());
    }

    public void testExportAnnotatedBeanWithDefaults() {
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedRemoteBean");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", AnnotatedRemoteBean.class.getName(), rd.getSource());
    }
    
    public void testExportAnnotatedBeanFromParentContextWithDefaults() {
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedRemoteBean3");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", MyService1.class.getName(), rd.getSource());
    }

    public void testExportAnnotatedXmlConfiguredBeanWithDefaults() {
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        flex.messaging.services.remoting.RemotingDestination rd = (flex.messaging.services.remoting.RemotingDestination) rs.getDestination("annotatedRemoteBean1");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", MyService1.class.getName(), rd.getSource());
    }

    @SuppressWarnings("unchecked")
    public void testExportBeanWithCustomSettings() {
        this.broker = (MessageBroker) getApplicationContext().getBean("remoteServiceBroker", MessageBroker.class);
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

    public static class MyDependency {
    }

    @RemotingDestination
    public static class MyService1 {
    }

    @RemotingDestination(value = "exportedAnnotatedRemoteBean2", messageBroker = "remoteServiceBroker", channels = { "my-amf", "my-secure-amf" }, serviceAdapter = "customAdapter1")
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

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "classpath:org/springframework/flex/config/remote-service.xml" };
    }
}
