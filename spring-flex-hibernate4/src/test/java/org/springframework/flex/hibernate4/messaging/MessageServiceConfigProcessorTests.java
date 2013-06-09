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

package org.springframework.flex.hibernate4.messaging;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.hibernate4.core.AbstractMessageBrokerTests;
import org.springframework.flex.messaging.MessageServiceConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.services.MessageService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class MessageServiceConfigProcessorTests extends AbstractMessageBrokerTests {

    private String servicesConfigPath;

    private @Mock
    BeanFactory beanFactory;

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    public void testMessageServiceAddedWithBrokerDefaultChannels() throws Exception {
        setDirty();
        addStartupProcessor(new MessageServiceConfigProcessor());
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate4/messaging/default-channels-config.xml";

        MessageService messageService = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());

        assertTrue("The broker's default channel was not assigned to the MessageService", messageService.getDefaultChannels().contains(
            "my-default-amf"));
        assertEquals("The default adapter was not set", "actionscript", messageService.getDefaultAdapter());
    }

    @SuppressWarnings("rawtypes")
	public void testMessageServiceAddedWithCustomDefaults() throws Exception {
        setDirty();
        MessageServiceConfigProcessor processor = new MessageServiceConfigProcessor();
        processor.setBeanFactory(this.beanFactory);
        processor.setDefaultAdapterId("my-adapter");
        processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
        addStartupProcessor(processor);
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate4/messaging/default-channels-config.xml";

        when((Class) this.beanFactory.getType("my-adapter")).thenReturn(TestAdapter.class);

        MessageService messageService = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());
        assertTrue("The default channel was not set", messageService.getDefaultChannels().contains("my-custom-default-amf"));
        assertEquals("The default adapter was not set", "my-adapter", messageService.getDefaultAdapter());
    }

    public void testMessageServiceAddedWithInferredDefaultChannels() throws Exception {
        setDirty();
        addStartupProcessor(new MessageServiceConfigProcessor());
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate4/messaging/inferred-default-channels-config.xml";

        MessageService messageService = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());
        assertTrue("The default channel was not determined", messageService.getDefaultChannels().contains("my-inferred-default-amf"));
        assertEquals("The default adapter was not set", "actionscript", messageService.getDefaultAdapter());
    }

    public void testMessageServiceAddedWithInvalidCustomChannels() throws Exception {
        setDirty();
        MessageServiceConfigProcessor processor = new MessageServiceConfigProcessor();
        processor.setDefaultChannels(new String[] { "my-bogus-channel" });
        addStartupProcessor(processor);
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate4/messaging/default-channels-config.xml";

        try {
            getMessageBroker();
            fail("Invalid channels not detected");
        } catch (BeanInitializationException ex) {
            // expected
            assertTrue("IllegalArgumentException expected", ex.getCause() instanceof IllegalArgumentException);
            setDirty();
        }
    }

    public void testMessageServiceAddedWithInvalidDefaultId() throws Exception {
        setDirty();
        MessageServiceConfigProcessor processor = new MessageServiceConfigProcessor();
        processor.setBeanFactory(this.beanFactory);
        processor.setDefaultAdapterId("my-adapter");
        processor.setDefaultChannels(new String[] { "my-custom-default-amf" });
        addStartupProcessor(processor);
        this.servicesConfigPath = "classpath:org/springframework/flex/hibernate4/messaging/default-channels-config.xml";

        try {
            getMessageBroker().getServiceByType(MessageService.class.getName());
            fail("An error should be thrown.");
        } catch (BeanInitializationException ex) {
            // expected
            assertTrue("IllegalArgumentException expected", ex.getCause() instanceof IllegalArgumentException);
            setDirty();
        }
    }

    public void testMessageServiceExists() throws Exception {
        setDirty();
        MessageServiceChecker checker = new MessageServiceChecker();
        addStartupProcessor(checker);
        addStartupProcessor(new MessageServiceConfigProcessor());
        this.servicesConfigPath = super.getServicesConfigPath();

        getMessageBroker();

        assertTrue("Processors not invoked", checker.beforeInvoked && checker.afterInvoked);
        assertSame("Pre-configured MessageService should be unmodified", checker.beforeMessageService, checker.afterMessageService);
    }

    @Override
    protected String getServicesConfigPath() {
        return this.servicesConfigPath;
    }

    private static class MessageServiceChecker implements MessageBrokerConfigProcessor {

        protected MessageService beforeMessageService;

        protected MessageService afterMessageService;

        protected boolean beforeInvoked = false;

        protected boolean afterInvoked = false;

        public MessageBroker processAfterStartup(MessageBroker broker) {
            this.afterInvoked = true;
            this.afterMessageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
            assertNotNull(this.afterMessageService);
            return broker;
        }

        public MessageBroker processBeforeStartup(MessageBroker broker) {
            this.beforeInvoked = true;
            this.beforeMessageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
            assertNotNull(this.beforeMessageService);
            return broker;
        }

    }

    private static class TestAdapter extends JavaAdapter {
    }
}
