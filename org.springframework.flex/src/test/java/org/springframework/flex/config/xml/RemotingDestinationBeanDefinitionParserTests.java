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

package org.springframework.flex.config.xml;

import java.util.Arrays;
import java.util.Iterator;

import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.BeanIds;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class RemotingDestinationBeanDefinitionParserTests extends AbstractFlexConfigurationTests {

    private MessageBroker broker;

    @SuppressWarnings("unchecked")
    public void testExportBeanWithCustomSettings() {
        this.broker = (MessageBroker) getApplicationContext().getBean("remoteServiceBroker", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        RemotingDestination rd = (RemotingDestination) rs.getDestination("exportedRemoteBean1");
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

    public void testExportBeanWithDefaults() {
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        RemotingDestination rd = (RemotingDestination) rs.getDestination("remoteBean1");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", Bean1.class.getName(), rd.getSource());
    }
    
    public void testExportScopedBeanWithDefaults() {
        RequestContextHolder.setRequestAttributes(new DispatcherServletWebRequest(new MockHttpServletRequest()));
        
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        RemotingService rs = (RemotingService) this.broker.getService("remoting-service");
        assertNotNull("Could not find the remoting service", rs);
        RemotingDestination rd = (RemotingDestination) rs.getDestination("scopedBean");
        assertNotNull("Destination not found", rd);
        assertEquals("Source not properly set", Bean1.class.getName(), rd.getSource());
        Bean1 bean = (Bean1) rd.getFactoryInstance().lookup();
        assertEquals("bar", bean.bar());
        
        RequestContextHolder.resetRequestAttributes();
    }

    public void testInvalidConfig() {
        try {
            new ClassPathXmlApplicationContext("org/springframework/flex/config/invalid-remote-service.xml");
            fail("Invalid message-broker config was not caught");
        } catch (BeanDefinitionParsingException ex) {
            // Expected
        }
    }

    /**
     * Uncomment this only for faster dev time testing
     */
//    @Override
//    protected String[] getConfigLocations() {
//        return new String[] { "classpath:org/springframework/flex/config/remote-service.xml",
//            "classpath:org/springframework/flex/config/remote-service-decorator.xml" };
//    }

    public static class Bean1 {

        public String bar() {
            return "bar";
        }

        public String baz() {
            return "baz";
        }

        public String foo() {
            return "foo";
        }

        public String zoo() {
            return "zoo";
        }
    }

    public static final class TestAdapter extends JavaAdapter {
    }
}
