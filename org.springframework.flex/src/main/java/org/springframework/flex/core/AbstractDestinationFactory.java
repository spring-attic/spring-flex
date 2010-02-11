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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.services.Service;
import flex.messaging.services.ServiceAdapter;

/**
 * Base class for BlazeDS destination factories.
 * 
 * @author Jeremy Grelle
 * @author Mark Fisher
 */
public abstract class AbstractDestinationFactory implements InitializingBean, DisposableBean, BeanNameAware, BeanFactoryAware {

    private volatile String destinationId;

    private volatile String beanName;

    private String[] channels;

    private MessageBroker broker;

    private BeanFactory beanFactory;

    private String serviceAdapter;

    /**
     * 
     * {@inheritDoc}
     */
    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(this.broker, "The 'messageBroker' property is required.");
        Destination destination = this.createDestination(getDestinationId(), this.broker);
        this.configureAdapter(destination);
        this.configureChannels(destination);
        this.initializeDestination(destination);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public final void destroy() throws Exception {
        if (this.broker == null || !this.broker.isStarted()) {
            return;
        }
        this.destroyDestination(getDestinationId(), this.broker);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * Specify the BlazeDS channel ids (in order of preference) for communication with this destination
     * 
     * @param channels an array of BlazeDS channel ids
     */
    public void setChannels(String[] channels) {
        this.channels = StringUtils.trimArrayElements(channels);
    }

    /**
     * Specify the id for the destination
     * 
     * @param destinationId the id to set
     */
    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    /**
     * Set the {@link MessageBroker} where this destination will be created.
     * 
     * @param broker the message broker for this destination
     */
    public void setMessageBroker(MessageBroker broker) {
        this.broker = broker;
    }

    /**
     * Specify a custom service adapter id to be used by this destination
     * 
     * @param serviceAdapter the custom service adapter id
     */
    public void setServiceAdapter(String serviceAdapter) {
        this.serviceAdapter = serviceAdapter;
    }

    /**
     * Configure the service adapter for the destination.
     * 
     * <p>
     * This implementation will first search the {@link BeanFactory} for a bean with a matching id and use it if found.
     * Otherwise the normal <code>createAdapter</code> method on the destination will be called.
     * 
     * <p>
     * May be overridden by subclasses that wish to specify custom adapter creation logic.
     * 
     * @param destination the destination being created
     */
    protected void configureAdapter(Destination destination) {
        String adapterId = StringUtils.hasText(this.serviceAdapter) ? this.serviceAdapter : getTargetService(this.broker).getDefaultAdapter();
        if (this.beanFactory.containsBean(adapterId)) {
            ServiceAdapter adapter = (ServiceAdapter) this.beanFactory.getBean(adapterId, ServiceAdapter.class);
            destination.setAdapter(adapter);
        } else if (destination.getAdapter() == null) {
            destination.createAdapter(adapterId);
        }
    }

    /**
     * Create a specific destination and add it to the {@link MessageBroker}
     * 
     * @param destinationId the id of the destination to create
     * @param broker the {@link MessageBroker} where the destination should be created
     * @return the created destination
     * @throws Exception if the destination could not be created successfully
     */
    protected abstract Destination createDestination(String destinationId, MessageBroker broker) throws Exception;

    /**
     * Stops and removes the specified destination from the {@link MessageBroker}
     * 
     * @param destinationId the id of the destination being destroyed
     * @param broker the {@link MessageBroker} from which the destination must be removed
     * @throws Exception if the destination could not be destroyed successfully
     */
    protected abstract void destroyDestination(String destinationId, MessageBroker broker) throws Exception;

    /**
     * Expose the BeanFactory to subclasses
     * 
     * @return the BeanFactory
     */
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }
    
    /**
     * Returns the id for the destination created by this factory
     * 
     * @return the destination id
     */
    protected String getDestinationId() {
        return StringUtils.hasText(this.destinationId) ? this.destinationId : this.beanName;
    }

    /**
     * Returns the target {@link Service} that will manage the destination
     * 
     * @param broker the {@link MessageBroker} that controls the service
     * @return the service
     */
    protected abstract Service getTargetService(MessageBroker broker);

    /**
     * Perform any necessary initialization logic on the created {@link Destination}
     * 
     * @param destination the created destination
     * @throws Exception if initialization fails
     */
    protected abstract void initializeDestination(Destination destination) throws Exception;

    private void configureChannels(Destination destination) {
        if (ObjectUtils.isEmpty(this.channels)) {
            return;
        }
        for (String channelId : this.channels) {
            Assert.isTrue(this.broker.getChannelIds().contains(channelId), "The channel " + channelId + " is not known to the MessageBroker "
                + this.broker.getId() + " and cannot be set on the destination " + getDestinationId());
        }
        destination.setChannels(CollectionUtils.arrayToList(this.channels));
    }

}
