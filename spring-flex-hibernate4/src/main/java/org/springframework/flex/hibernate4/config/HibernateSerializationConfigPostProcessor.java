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

package org.springframework.flex.hibernate4.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.core.io.AbstractAmfConversionServiceConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link BeanFactoryPostProcessor} that will automatically configure Hibernate AMF serialization support if:
 * <ol>
 *     <li>Hibernate is detected on the classpath</li>
 *     <li>An instance of {@link HibernateConfigProcessor} has not been manually configured.
 * </ol>
 *
 * @author Jeremy Grelle
 */
public class HibernateSerializationConfigPostProcessor implements BeanFactoryPostProcessor {

    private static final Log log = LogFactory.getLog(HibernateSerializationConfigPostProcessor.class);
    
    private static final String HIBERNATE_CONFIG_PROCESSOR_CLASS = "org.springframework.flex.hibernate4.config.HibernateConfigProcessor";
    
    private static final String JPA_HIBERNATE_CONFIG_PROCESSOR_CLASS = "org.springframework.flex.hibernate4.config.JpaHibernateConfigProcessor";
    
    private static final String MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME = "org.springframework.flex.core.MessageBrokerFactoryBean";
    
    private static final String CONFIG_PROCESSORS_PROPERTY = "configProcessors";
    
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        
        if (isHibernateDetected(beanFactory.getBeanClassLoader())) {
            
            //Make sure there is a MessageBrokerFactoryBean present
            BeanDefinition messageBrokerBeanDef = findMessageBrokerFactoryBeanDefinition(beanFactory);
            Assert.notNull("Could not find an appropriate bean definition for MessageBrokerBeanDefinitionFactoryBean.");
            
            MutablePropertyValues brokerProps = messageBrokerBeanDef.getPropertyValues();
            ManagedSet<RuntimeBeanReference> configProcessors;
            if(brokerProps.getPropertyValue(CONFIG_PROCESSORS_PROPERTY) != null) {
                configProcessors = (ManagedSet<RuntimeBeanReference>) brokerProps.getPropertyValue(CONFIG_PROCESSORS_PROPERTY).getValue();
            } else {
                configProcessors = new ManagedSet<RuntimeBeanReference>();
            }
            
            //Abort if HibernateConfigProcessor is already present
            if (isAmfConversionServiceProcessorConfigured(beanFactory, configProcessors)) {
                return;
            }
            
            if (!ClassUtils.isAssignableValue(BeanDefinitionRegistry.class, beanFactory)) {
                if (log.isWarnEnabled()) {
                    log.warn("Hibernate AMF serialization support could not be auto-configured because the current BeanFactory does not implement " +
                    		"BeanDefinitionRegistry.  In order for this support to be enabled, you must manually configure an instance of "+
                    		HIBERNATE_CONFIG_PROCESSOR_CLASS);
                }
            }            

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            
            //Add the appropriate HibernateConfigProcessor bean definition
            BeanDefinitionBuilder processorBuilder;
            if (isJpaDetected(beanFactory.getBeanClassLoader())) {
                processorBuilder = BeanDefinitionBuilder.rootBeanDefinition(JPA_HIBERNATE_CONFIG_PROCESSOR_CLASS);
            } else {
                processorBuilder = BeanDefinitionBuilder.rootBeanDefinition(HIBERNATE_CONFIG_PROCESSOR_CLASS);
            }
            
            String processorId = BeanDefinitionReaderUtils.registerWithGeneratedName(processorBuilder.getBeanDefinition(), registry);
            
            configProcessors.add(new RuntimeBeanReference(processorId));
        }
    }

    private boolean isAmfConversionServiceProcessorConfigured(ConfigurableListableBeanFactory beanFactory, ManagedSet<RuntimeBeanReference> configProcessors) {
        
        for (RuntimeBeanReference configProcessor: configProcessors) {
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(configProcessor.getBeanName());
            if (bd instanceof AbstractBeanDefinition) {
                AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
                if (!abd.hasBeanClass()) {
                    try {
                        abd.resolveBeanClass(beanFactory.getBeanClassLoader());
                    } catch (ClassNotFoundException ex) {
                        throw new CannotLoadBeanClassException(abd.getResourceDescription(), configProcessor.getBeanName(), abd.getBeanClassName(), ex);
                    }
                }
                if (AbstractAmfConversionServiceConfigProcessor.class.isAssignableFrom(abd.getBeanClass())) {
                    return true;
                }                    
            }
        }
        return false;
    }
    
    private BeanDefinition findMessageBrokerFactoryBeanDefinition(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory.containsBeanDefinition(BeanIds.MESSAGE_BROKER)) {
            return beanFactory.getBeanDefinition(BeanIds.MESSAGE_BROKER);
        } else {
            for (String beanDefName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDef = beanFactory.getBeanDefinition(beanDefName);
                if (beanDef.getBeanClassName().equals(MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME)) {
                    return beanDef;
                }
            }
        }
        return null;
    }

    private boolean isHibernateDetected(ClassLoader classLoader) {
        return ClassUtils.isPresent("org.hibernate.SessionFactory", classLoader);
    }
    
    private boolean isJpaDetected(ClassLoader classLoader) {
        return ClassUtils.isPresent("javax.persistence.EntityManagerFactory", classLoader);
    }
}
