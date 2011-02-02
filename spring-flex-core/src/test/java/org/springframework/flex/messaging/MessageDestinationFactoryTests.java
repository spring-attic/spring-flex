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

package org.springframework.flex.messaging;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.beans.PropertyEditor;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.flex.config.json.JsonConfigMapPropertyEditor;
import org.springframework.flex.core.AbstractMessageBrokerTests;

import edu.emory.mathcs.backport.java.util.Arrays;
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.security.LoginManager;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

public class MessageDestinationFactoryTests extends AbstractMessageBrokerTests {

    MessageDestinationFactory factory;

    MessageService service;

    @Mock
    private BeanFactory beanFactory;

    @Mock
    LoginManager loginManager;

    LoginManager originalLoginManager;

    @Override
    public void setUp() throws Exception {

        if (!getServicesConfigPath().equals(getCurrentConfigPath())) {
            setDirty();
        }

        MockitoAnnotations.initMocks(this);
        this.service = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());

        this.originalLoginManager = getMessageBroker().getLoginManager();
        getMessageBroker().setLoginManager(this.loginManager);
    }

    @Override
    public void tearDown() throws Exception {
        getMessageBroker().setLoginManager(this.originalLoginManager);
    }

    public void testDefaultDestinationCreated() throws Exception {

        this.factory = new MessageDestinationFactory();
        this.factory.setBeanName("foo1");
        this.factory.setMessageBroker(getMessageBroker());
        this.factory.setBeanFactory(this.beanFactory);

        this.factory.afterPropertiesSet();

        MessageDestination destination = (MessageDestination) this.service.getDestination("foo1");
        assertNotNull(destination);
        assertEquals("foo1", destination.getId());
        assertTrue(destination.isStarted());
        assertNotNull(destination.getAdapter());
        assertTrue(destination.getAdapter() instanceof ActionScriptAdapter);
        assertTrue(destination.getAdapter().isStarted());
    }

    public void testDestinationWithExplicitProperties() throws Exception {

        this.factory = new MessageDestinationFactory();
        this.factory.setBeanName("foo-factory");
        this.factory.setDestinationId("foo2");
        String[] channels = new String[] { "my-amf", "my-polling-amf" };
        this.factory.setChannels(channels);
        this.factory.setAllowSubtopics("true");
        this.factory.setClusterMessageRouting("broadcast");
        this.factory.setMessageTimeToLive("1");
        this.factory.setSubscriptionTimeoutMinutes("1");
        this.factory.setSubtopicSeparator("/");
        this.factory.setThrottleInboundMaxFrequency("500");
        this.factory.setThrottleInboundPolicy("ERROR");
        this.factory.setThrottleOutboundMaxFrequency("500");
        this.factory.setThrottleOutboundPolicy("IGNORE");

        this.factory.setMessageBroker(getMessageBroker());
        this.factory.setBeanFactory(this.beanFactory);

        this.factory.afterPropertiesSet();

        MessageDestination destination = (MessageDestination) this.service.getDestination("foo2");
        assertNotNull(destination);
        assertEquals("foo2", destination.getId());
        assertTrue(destination.getChannels().containsAll(Arrays.asList(channels)));
        assertTrue(destination.getServerSettings().getAllowSubtopics());
        assertTrue(destination.getServerSettings().isBroadcastRoutingMode());
        assertEquals(1, destination.getServerSettings().getMessageTTL());
        assertEquals(1, destination.getNetworkSettings().getSubscriptionTimeoutMinutes());
        assertEquals("/", destination.getServerSettings().getSubtopicSeparator());
        assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getIncomingDestinationFrequency());
        assertEquals(ThrottleSettings.parsePolicy("ERROR"), destination.getNetworkSettings().getThrottleSettings().getInboundPolicy());
        assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getOutgoingDestinationFrequency());
        assertEquals(ThrottleSettings.parsePolicy("IGNORE"), destination.getNetworkSettings().getThrottleSettings().getOutboundPolicy());

    }

    public void testDestinationWithJsonConfigMap() throws Exception {

        PropertyEditor editor = new JsonConfigMapPropertyEditor();
        editor.setAsText(readJsonFile());
        this.factory = new MessageDestinationFactory((ConfigMap) editor.getValue());
        this.factory.setBeanName("foo-factory");
        this.factory.setDestinationId("foo4");
        String[] channels = new String[] { "my-amf", "my-polling-amf" };
        this.factory.setChannels(channels);

        this.factory.setMessageBroker(getMessageBroker());
        this.factory.setBeanFactory(this.beanFactory);

        this.factory.afterPropertiesSet();

        MessageDestination destination = (MessageDestination) this.service.getDestination("foo4");
        assertNotNull(destination);
        assertEquals("foo4", destination.getId());
        assertTrue(destination.getChannels().containsAll(Arrays.asList(channels)));
        assertTrue(destination.getServerSettings().getAllowSubtopics());
        assertTrue(destination.getServerSettings().isBroadcastRoutingMode());
        assertEquals(1, destination.getServerSettings().getMessageTTL());
        assertEquals(1, destination.getNetworkSettings().getSubscriptionTimeoutMinutes());
        assertEquals("/", destination.getServerSettings().getSubtopicSeparator());
        assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getIncomingDestinationFrequency());
        assertEquals(ThrottleSettings.parsePolicy("ERROR"), destination.getNetworkSettings().getThrottleSettings().getInboundPolicy());
        assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getOutgoingDestinationFrequency());
        assertEquals(ThrottleSettings.parsePolicy("IGNORE"), destination.getNetworkSettings().getThrottleSettings().getOutboundPolicy());

    }

    public void testDestinationWithSecurityConstraints() throws Exception {
        this.factory = new MessageDestinationFactory();
        this.factory.setBeanName("foo3");
        this.factory.setSendSecurityConstraint("spring-security-users");
        this.factory.setSubscribeSecurityConstraint("spring-security-users");

        this.factory.setMessageBroker(getMessageBroker());
        this.factory.setBeanFactory(this.beanFactory);

        this.factory.afterPropertiesSet();

        MessageDestination destination = (MessageDestination) this.service.getDestination("foo3");
        assertNotNull(destination);
        assertEquals("foo3", destination.getId());

        MessagingAdapter adapter = (MessagingAdapter) destination.getAdapter();
        adapter.getSecurityConstraintManager().assertSendAuthorization();
        adapter.getSecurityConstraintManager().assertSubscribeAuthorization();

        verify(this.loginManager, times(2)).checkConstraint(isA(SecurityConstraint.class));
    }

    private String readJsonFile() throws Exception {
        Resource jsonFile = new DefaultResourceLoader().getResource("classpath:org/springframework/flex/messaging/MessageDestinationProps.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(jsonFile.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

}
