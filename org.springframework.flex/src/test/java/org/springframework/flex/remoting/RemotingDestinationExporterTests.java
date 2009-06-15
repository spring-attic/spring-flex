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

package org.springframework.flex.remoting;

import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.util.CollectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class RemotingDestinationExporterTests extends AbstractMessageBrokerTests {

    private static final String DEFAULT_SERVICE_ID = "myServiceExporter";

    RemotingDestinationExporter exporter;

    Object testService = new StubService();

    private @Mock
    BeanFactory beanFactory;

    @Override
    public void setUp() throws Exception {
        if (getCurrentConfigPath() != getServicesConfigPath()) {
            setDirty();
        }
        configureExporter();
        MockitoAnnotations.initMocks(this);
        this.exporter.setBeanFactory(this.beanFactory);
    }

    @Override
    public void tearDown() throws Exception {
        this.exporter.destroy();
    }

    public void testDestinationConfiguredWithInvalidChannels() throws Exception {
        String[] channelIds = new String[] { "my-fubar" };
        this.exporter.setChannels(channelIds);

        try {
            this.exporter.afterPropertiesSet();
            fail("Invalid channel not detected");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDestinationConfiguredWithInvalidExcludeMethods() throws Exception {

        String methodName = "retreiveStringValues";
        this.exporter.setExcludeMethods(new String[] { methodName });
        try {
            this.exporter.afterPropertiesSet();
            fail("Invalid exclude method not detected.");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDestinationConfiguredWithInvalidIncludeMethods() throws Exception {

        String methodName = "retreiveStringValues";
        this.exporter.setIncludeMethods(new String[] { methodName });
        try {
            this.exporter.afterPropertiesSet();
            fail("Invalid include method not detected.");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDestinationConfiguredWithNullMessageBroker() throws Exception {

        this.exporter.setMessageBroker(null);
        try {
            this.exporter.afterPropertiesSet();
            fail("Invalid MessageBroker not detected.");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDestinationConfiguredWithNullService() throws Exception {

        this.exporter.setService(null);
        try {
            this.exporter.afterPropertiesSet();
            fail("Invalid service not detected.");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDestinationConfiguredWithSpringManagedCustomAdapter() throws Exception {

        String adapterId = "my-adapter";
        ServiceAdapter springAdapter = new TestAdapter();
        springAdapter.setId(adapterId);
        when(this.beanFactory.containsBean(adapterId)).thenReturn(true);
        when(this.beanFactory.getBean(adapterId, ServiceAdapter.class)).thenReturn(springAdapter);

        RemotingService remotingService = getRemotingService();

        this.exporter.setServiceAdapter(adapterId);
        this.exporter.afterPropertiesSet();

        RemotingDestination remotingDestination = (RemotingDestination) remotingService.getDestination(DEFAULT_SERVICE_ID);

        assertSame(springAdapter, remotingDestination.getAdapter());
    }

    public void testDestinationConfiguredWithSpringManagedDefaultAdapter() throws Exception {

        String adapterId = "java-object";
        ServiceAdapter springAdapter = new TestAdapter();
        springAdapter.setId(adapterId);
        when(this.beanFactory.containsBean(adapterId)).thenReturn(true);
        when(this.beanFactory.getBean(adapterId, ServiceAdapter.class)).thenReturn(springAdapter);

        RemotingService remotingService = getRemotingService();

        this.exporter.afterPropertiesSet();

        RemotingDestination remotingDestination = (RemotingDestination) remotingService.getDestination(DEFAULT_SERVICE_ID);

        assertSame(springAdapter, remotingDestination.getAdapter());
    }

    @SuppressWarnings("unchecked")
    public void testDestinationConfiguredWithValidChannels() throws Exception {
        RemotingService remotingService = getRemotingService();

        String[] channelIds = new String[] { "my-secure-amf" };
        this.exporter.setChannels(channelIds);

        this.exporter.afterPropertiesSet();

        RemotingDestination remotingDestination = (RemotingDestination) remotingService.getDestination(DEFAULT_SERVICE_ID);
        assertTrue("Custom channels not set", remotingDestination.getChannels().containsAll(CollectionUtils.arrayToList(channelIds)));
        assertFalse("Default channel not overriden", remotingDestination.getChannels().contains("my-amf"));
    }

    public void testDestinationConfiguredWithValidExcludeMethods() throws Exception {

        RemotingService remotingService = getRemotingService();

        String methodName = "retreiveStringValue";
        this.exporter.setExcludeMethods(new String[] { methodName });
        this.exporter.afterPropertiesSet();

        assertTrue("The remoting destination was not configured with a JavaAdapter",
            remotingService.getDestination(DEFAULT_SERVICE_ID).getAdapter() instanceof JavaAdapter);
        JavaAdapter adapter = (JavaAdapter) remotingService.getDestination(DEFAULT_SERVICE_ID).getAdapter();
        Iterator<?> i = adapter.getExcludeMethodIterator();
        RemotingMethod method = (RemotingMethod) i.next();
        assertEquals("Exclude method not properly configured", methodName, method.getName());

    }

    public void testDestinationConfiguredWithValidIncludeMethods() throws Exception {

        RemotingService remotingService = getRemotingService();

        String methodName = "retreiveStringValue";
        this.exporter.setIncludeMethods(new String[] { methodName });
        this.exporter.afterPropertiesSet();

        assertTrue("The remoting destination was not configured with a JavaAdapter",
            remotingService.getDestination(DEFAULT_SERVICE_ID).getAdapter() instanceof JavaAdapter);
        JavaAdapter adapter = (JavaAdapter) remotingService.getDestination(DEFAULT_SERVICE_ID).getAdapter();
        Iterator<?> i = adapter.getIncludeMethodIterator();
        RemotingMethod method = (RemotingMethod) i.next();
        assertEquals("Include method not properly configured", methodName, method.getName());

    }

    public void testDestinationRegisteredWithDefaultConfig() throws Exception {

        RemotingService remotingService = getRemotingService();

        this.exporter.afterPropertiesSet();

        RemotingDestination remotingDestination = (RemotingDestination) remotingService.getDestination(DEFAULT_SERVICE_ID);
        assertNotNull("RemotingDestination not registered", remotingDestination);
        assertTrue("RemotingDestination not started", remotingDestination.isStarted());
        assertEquals("Default adapter not set", "java-object", remotingDestination.getAdapter().getId());
        assertTrue("No channels set on destination", remotingDestination.getChannels().size() > 0);
        assertEquals("Source should be class name of target object", this.testService.getClass().getName(), remotingDestination.getSource());
    }

    public void testDestinationRegisteredWithDestinationId() throws Exception {

        RemotingService remotingService = getRemotingService();

        String destinationId = "myService";
        this.exporter.setDestinationId(destinationId);
        this.exporter.afterPropertiesSet();

        assertNotNull("RemotingDestination not registered", remotingService.getDestination(destinationId));
    }

    private void configureExporter() throws Exception {

        this.exporter = new RemotingDestinationExporter();
        this.exporter.setBeanName(DEFAULT_SERVICE_ID);
        this.exporter.setMessageBroker(getMessageBroker());
        this.exporter.setService(this.testService);
    }

    private RemotingService getRemotingService() throws Exception {
        MessageBroker broker = getMessageBroker();

        return (RemotingService) broker.getServiceByType(RemotingService.class.getName());
    }

    private static final class StubService {

        public String retreiveStringValue() {
            return "foo";
        }
    }

    private static final class TestAdapter extends JavaAdapter {
    }
}
