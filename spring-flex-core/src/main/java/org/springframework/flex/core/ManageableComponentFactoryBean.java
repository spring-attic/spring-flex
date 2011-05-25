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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.remoting.adapters.JavaAdapter;

/**
 * {@link FactoryBean} that allows for the creation of BlazeDS {@link ManageableComponent} prototype instances with the
 * appropriate {@link ManageableComponent#initialize(String, ConfigMap)} callback after creation. Useful for configuring
 * non-singleton helper objects such as a custom {@link JavaAdapter}.
 * 
 * @author Jeremy Grelle
 */
public class ManageableComponentFactoryBean implements FactoryBean<ManageableComponent>, BeanNameAware {

    private ConfigMap properties = new ConfigMap();

    private String beanName;

    private final Class<? extends ManageableComponent> componentClass;

    /**
     * Creates a new ManageableComponentFactoryBean for the specified component class
     * 
     * @param componentClass the class of the component this {@link FactoryBean} will create
     */
    public ManageableComponentFactoryBean(Class<? extends ManageableComponent> componentClass) {
        this.componentClass = componentClass;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public ManageableComponent getObject() throws Exception {
        ManageableComponent component = (ManageableComponent) BeanUtils.instantiateClass(this.componentClass);
        component.setId(this.beanName);
        component.initialize(this.beanName, this.properties);
        return component;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Class<?> getObjectType() {
        return this.componentClass;
    }

    /**
     * It is expected that objects created by this factory will always be prototype instances.
     */
    public final boolean isSingleton() {
        return false;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * Sets the properties {@link ConfigMap} to use in initializing the created component
     * 
     * @param properties the properties map
     */
    public void setProperties(ConfigMap properties) {
        this.properties = properties;
    }

}
