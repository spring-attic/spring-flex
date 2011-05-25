/*
 * Copyright 2002-2011 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import flex.messaging.MessageBroker;
import flex.messaging.services.Service;
import flex.messaging.services.ServiceAdapter;

/**
 * Base {@link MessageBrokerConfigProcessor} implementation for handling automatic {@link Service} registration with the
 * {@link MessageBroker}
 * 
 * @author Jeremy Grelle
 */
public abstract class AbstractServiceConfigProcessor implements MessageBrokerConfigProcessor, BeanFactoryAware {

    private String defaultAdapterId;

    private String[] defaultChannels;

    private BeanFactory beanFactory;

    public AbstractServiceConfigProcessor() {
        this.defaultAdapterId = getServiceAdapterId();
    }

    /**
     * Error checking is done on the started MessageBroker to ensure configuration was successful.
     * 
     * @see MessageBrokerConfigProcessor#processAfterStartup(MessageBroker)
     */
    public MessageBroker processAfterStartup(MessageBroker broker) {
        // Eagerly detect possible problems with the Service
        Service service = broker.getServiceByType(getServiceClassName());
        Assert.notNull(service, "The MessageBroker with id '" + broker.getId() + "' does not have a service of type " + getServiceClassName()
            + " configured.");
        Assert.isTrue(service.isStarted(), "The Service with id '" + service.getId() + "' of MessageBroker with id '" + broker.getId()
            + "' was not started as expected.");
        return broker;
    }

    /**
     * The MessageBroker is checked to see if the Service has already been configured via the BlazeDS XML config. If no
     * existing Service is found, one will be installed using the defined configuration properties of this class.
     * 
     * @see MessageBrokerConfigProcessor#processBeforeStartup(MessageBroker)
     */
    public MessageBroker processBeforeStartup(MessageBroker broker) {
        Service service = broker.getServiceByType(getServiceClassName());
        if (service == null) {
            service = broker.createService(getServiceId(), getServiceClassName());
            if (getServiceAdapterId().equals(this.defaultAdapterId)) {
                service.registerAdapter(getServiceAdapterId(), getServiceAdapterClassName());
            } else {
                Assert.isAssignable(ServiceAdapter.class, this.beanFactory.getType(this.defaultAdapterId),
                    "A custom default adapter id must refer to a valid Spring bean that " + "is a subclass of " + ServiceAdapter.class.getName()
                        + ".  ");
                service.registerAdapter(this.defaultAdapterId, CustomSpringAdapter.class.getName());
            }
            service.setDefaultAdapter(this.defaultAdapterId);
            if (!ObjectUtils.isEmpty(this.defaultChannels)) {
                addDefaultChannels(broker, service);
            } else {
                findDefaultChannel(broker, service);
            }
        }
        return broker;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Set the id for the default adapter to be installed in the {@link Service}.
     * 
     * @param defaultAdapterId the id to set for the adapter
     */
    public void setDefaultAdapterId(String defaultAdapterId) {
        this.defaultAdapterId = defaultAdapterId;
    }

    /**
     * Set the ids of the default channels to be set on the {@link Service}. If not set, the application-wide defaults
     * will be used. If no application-wide defaults can be found, a best guess will be made using the first available
     * channel with an appropriate endpoint.
     * 
     * @param defaultChannels the ids of the default channels for the {@link Service}
     */
    public void setDefaultChannels(String[] defaultChannels) {
        this.defaultChannels = StringUtils.trimArrayElements(defaultChannels);
    }

    /**
     * Find and set an appropriate default channel for the {@link Service}
     * 
     * @param broker the {@link MessageBroker} that controls the {@link Service}
     * @param service the service being configured
     */
    protected abstract void findDefaultChannel(MessageBroker broker, Service service);

    /**
     * Returns the class name of the default {@link ServiceAdapter} for the {@link Service}
     * 
     * @return the default adapter class name
     */
    protected abstract String getServiceAdapterClassName();

    /**
     * Returns the default {@link ServiceAdapter} id for the {@link Service}
     * 
     * @return the default adapter id
     */
    protected abstract String getServiceAdapterId();

    /**
     * Returns the class name of the specific {@link Service} implementation being configured
     * 
     * @return the service class name
     */
    protected abstract String getServiceClassName();

    /**
     * Returns the default id for the {@link Service} being configured
     * 
     * @return the default service id
     */
    protected abstract String getServiceId();

    /**
     * Adds the default channels to the {@link Service} being configured. The <code>defaultChannels</code> will be
     * validated to ensure they exist in the {@link MessageBroker} before they are set.
     * 
     * @param broker the newly configured MessageBroker
     * @param remotingService the newly created Service
     */
    private void addDefaultChannels(MessageBroker broker, Service service) {
        List<String> defaultChannelList = new ArrayList<String>();
        for (String channelId : this.defaultChannels) {
            Assert.isTrue(broker.getChannelIds().contains(channelId), "The channel " + channelId + " is not known to the MessageBroker "
                + broker.getId() + " and cannot be set as a default channel" + " on the " + getServiceClassName());
            defaultChannelList.add(channelId);
        }
        service.setDefaultChannels(defaultChannelList);
    }

    /**
     * This is simply a marker to denote that a Spring-managed adapter will be injected at the proper initialization
     * point.
     */
    protected static final class CustomSpringAdapter {

        public CustomSpringAdapter() {
            throw new UnsupportedOperationException("This adapter class should never be instantiated directly by BlazeDS.  "
                + "It is only a placeholder to denote that a Spring-managed adapter should be injected when a Destination is" + "initialized.");
        }
    }

}
