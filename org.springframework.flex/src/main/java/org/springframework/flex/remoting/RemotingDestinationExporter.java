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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.flex.core.AbstractDestinationFactory;
import org.springframework.flex.core.MessageBrokerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import flex.messaging.Destination;
import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.RemotingService;
import flex.messaging.services.Service;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

/**
 * An factory for exposing a Spring-managed bean to a Flex client for direct remoting calls.
 * 
 * <p>
 * The exported service will be exposed to the Flex client as a BlazeDS remoting service destination. By default, the
 * destination id will be the same as the bean name of this factory. This may be overridden using the serviceId
 * property. <i>Note that this convention is slightly different from that employed by the <code>remote-service</code>
 * xml config tag. See the xsd docs for details.</i>
 * 
 * <p>
 * The methods on the exported service that are exposed to the Flex client can be controlled using the includeMethods
 * and excludeMethods properties.
 * </p>
 * 
 * @see MessageBrokerFactoryBean
 * 
 * @author Jeremy Grelle
 * @author Mark Fisher
 */
public class RemotingDestinationExporter extends AbstractDestinationFactory implements FlexFactory {

    private static final Log log = LogFactory.getLog(RemotingDestinationExporter.class);

    private Object service;

    private String[] includeMethods;

    private String[] excludeMethods;

    /**
     * 
     * {@inheritDoc}
     */
    public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
        return new ServiceFactoryInstance(this, id, properties);
    }

    /**
     * @exclude
     */
    public void initialize(String id, ConfigMap configMap) {
        // No-op
    }

    /**
     * Lookup will be handled directly by the created FactoryInstance
     * 
     * @exclude
     */
    public Object lookup(FactoryInstance instanceInfo) {
        throw new UnsupportedOperationException("FlexFactory.lookup");
    }

    /**
     * Sets the methods to be excluded from the bean being exported
     * 
     * @param excludeMethods the methods to exclude
     */
    public void setExcludeMethods(String[] excludeMethods) {
        this.excludeMethods = StringUtils.trimArrayElements(excludeMethods);
    }

    /**
     * Sets the methods to included from the bean being exported
     * 
     * @param includeMethods the methods to include
     */
    public void setIncludeMethods(String[] includeMethods) {
        this.includeMethods = StringUtils.trimArrayElements(includeMethods);
    }

    /**
     * Sets the bean being exported
     * 
     * @param service the bean being exported
     */
    public void setService(Object service) {
        this.service = service;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected Destination createDestination(String destinationId, MessageBroker broker) {
        Assert.notNull(this.service, "The 'service' property is required.");

        // Look up the remoting service
        RemotingService remotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());
        Assert.notNull(remotingService, "Could not find a proper RemotingService in the Flex MessageBroker.");

        // Register and start the destination
        RemotingDestination destination = (RemotingDestination) remotingService.createDestination(destinationId);

        destination.setFactory(this);

        if (log.isInfoEnabled()) {
            log.info("Created remoting destination with id '" + destinationId + "'");
        }

        return destination;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void destroyDestination(String destinationId, MessageBroker broker) {
        RemotingService remotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());

        if (remotingService == null) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("Removing remoting destination '" + destinationId + "'");
        }

        remotingService.removeDestination(destinationId);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected Service getTargetService(MessageBroker broker) {
        return broker.getServiceByType(RemotingService.class.getName());
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void initializeDestination(Destination destination) {
        destination.start();

        Assert.isInstanceOf(ServiceAdapter.class, destination.getAdapter(),
            "Spring beans exported as a RemotingDestination require a ServiceAdapter.");

        configureIncludes(destination);
        configureExcludes(destination);

        if (log.isInfoEnabled()) {
            log.info("Remoting destination '" + destination.getId() + "' has been started started successfully.");
        }
    }

    private void configureExcludes(Destination destination) {

        if (this.excludeMethods == null) {
            return;
        }

        JavaAdapter adapter = (JavaAdapter) destination.getAdapter();
        for (RemotingMethod method : getRemotingMethods(this.excludeMethods)) {
            adapter.addExcludeMethod(method);
        }
    }

    private void configureIncludes(Destination destination) {

        if (this.includeMethods == null) {
            return;
        }

        JavaAdapter adapter = (JavaAdapter) destination.getAdapter();
        for (RemotingMethod method : getRemotingMethods(this.includeMethods)) {
            adapter.addIncludeMethod(method);
        }
    }

    private List<RemotingMethod> getRemotingMethods(String[] methodNames) {
        List<RemotingMethod> remotingMethods = new ArrayList<RemotingMethod>();
        for (String name : methodNames) {
            Assert.isTrue(ClassUtils.hasAtLeastOneMethodWithName(this.service.getClass(), name), "Could not find method with name '" + name
                + "' on the exported service of type " + this.service.getClass());
            RemotingMethod method = new RemotingMethod();
            method.setName(name);
            remotingMethods.add(method);
        }
        return remotingMethods;
    }

    private final class ServiceFactoryInstance extends FactoryInstance {

        public ServiceFactoryInstance(FlexFactory factory, String id, ConfigMap properties) {
            super(factory, id, properties);
        }

        @Override
        public Object lookup() {
            return RemotingDestinationExporter.this.service;
        }
    }
}
