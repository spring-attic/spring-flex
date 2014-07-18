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

package org.springframework.flex.hibernate3.remoting;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.hibernate3.core.AbstractMessageBrokerTests;
import org.springframework.flex.remoting.RemotingServiceConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class RemotingServiceConfigProcessorTests extends AbstractMessageBrokerTests {

    private String servicesConfigPath;

    private @Mock
    BeanFactory beanFactory;

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    public void testRemotingServiceAddedWithBrokerDefaultChannels() throws Exception {
        setDirty();
        addStartupProcessor(new RemotingServiceConfigProcessor());
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate3/remoting/default-channels-config.xml";

        RemotingService remotingService = (RemotingService) getMessageBroker().getServiceByType(RemotingService.class.getName());

        assertTrue("The broker's default channel was not assigned to the RemotingService", remotingService.getDefaultChannels().contains(
            "my-default-amf"));
        assertEquals("The default adapter was not set", "java-object", remotingService.getDefaultAdapter());
    }

    @SuppressWarnings("rawtypes")
	public void testRemotingServiceAddedWithCustomDefaults() throws Exception {
        setDirty();
        RemotingServiceConfigProcessor processor = new RemotingServiceConfigProcessor();
        processor.setBeanFactory(this.beanFactory);
        processor.setDefaultAdapterId("my-adapter");
        processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
        addStartupProcessor(processor);
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate3/remoting/default-channels-config.xml";

        when((Class) this.beanFactory.getType("my-adapter")).thenReturn(TestAdapter.class);

        RemotingService remotingService = (RemotingService) getMessageBroker().getServiceByType(RemotingService.class.getName());
        assertTrue("The default channel was not set", remotingService.getDefaultChannels().contains("my-custom-default-amf"));
        assertEquals("The default adapter was not set", "my-adapter", remotingService.getDefaultAdapter());
    }

    public void testRemotingServiceAddedWithInferredDefaultChannels() throws Exception {
        setDirty();
        addStartupProcessor(new RemotingServiceConfigProcessor());
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate3/remoting/inferred-default-channels-config.xml";

        RemotingService remotingService = (RemotingService) getMessageBroker().getServiceByType(RemotingService.class.getName());
        assertTrue("The default channel was not determined", remotingService.getDefaultChannels().contains("my-inferred-default-amf"));
        assertEquals("The default adapter was not set", "java-object", remotingService.getDefaultAdapter());
    }

    public void testRemotingServiceAddedWithInvalidCustomChannels() throws Exception {
        setDirty();
        RemotingServiceConfigProcessor processor = new RemotingServiceConfigProcessor();
        processor.setDefaultChannels(new String[] { "my-bogus-channel" });
        addStartupProcessor(processor);
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate3/remoting/default-channels-config.xml";

        try {
            getMessageBroker();
            fail("Invalid channels not detected");
        } catch (BeanInitializationException ex) {
            // expected
            assertTrue("IllegalArgumentException expected", ex.getCause() instanceof IllegalArgumentException);
            setDirty();
        }
    }

    public void testRemotingServiceAddedWithInvalidDefaultId() throws Exception {
        setDirty();
        RemotingServiceConfigProcessor processor = new RemotingServiceConfigProcessor();
        processor.setBeanFactory(this.beanFactory);
        processor.setDefaultAdapterId("my-adapter");
        processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
        addStartupProcessor(processor);
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate3/remoting/default-channels-config.xml";

        try {
            getMessageBroker().getServiceByType(RemotingService.class.getName());
            fail("An error should be thrown.");
        } catch (BeanInitializationException ex) {
            // expected
            assertTrue("IllegalArgumentException expected", ex.getCause() instanceof IllegalArgumentException);
            setDirty();
        }
    }

    public void testRemotingServiceExists() throws Exception {
        setDirty();
        RemotingServiceChecker checker = new RemotingServiceChecker();
        addStartupProcessor(checker);
        addStartupProcessor(new RemotingServiceConfigProcessor());
        this.servicesConfigPath = super.getServicesConfigPath();

        getMessageBroker();

        assertTrue("Processors not invoked", checker.beforeInvoked && checker.afterInvoked);
        assertSame("Pre-configured RemotingService should be unmodified", checker.beforeRemotingService, checker.afterRemotingService);
    }

    @Override
    protected String getServicesConfigPath() {
        return this.servicesConfigPath;
    }

    private static class RemotingServiceChecker implements MessageBrokerConfigProcessor {

        protected RemotingService beforeRemotingService;

        protected RemotingService afterRemotingService;

        protected boolean beforeInvoked = false;

        protected boolean afterInvoked = false;

        public MessageBroker processAfterStartup(MessageBroker broker) {
            this.afterInvoked = true;
            this.afterRemotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());
            assertNotNull(this.afterRemotingService);
            return broker;
        }

        public MessageBroker processBeforeStartup(MessageBroker broker) {
            this.beforeInvoked = true;
            this.beforeRemotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());
            assertNotNull(this.beforeRemotingService);
            return broker;
        }

    }

    private static class TestAdapter extends JavaAdapter {
    }
}
