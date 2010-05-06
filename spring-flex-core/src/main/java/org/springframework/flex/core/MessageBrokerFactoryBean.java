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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.servlet.MessageBrokerHandlerAdapter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.DispatcherServlet;

import flex.management.MBeanLifecycleManager;
import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.VersionInfo;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.MessagingConfiguration;

/**
 * {@link FactoryBean} that creates a local {@link MessageBroker} instance within a Spring web application context. The
 * resulting Spring-managed MessageBroker can be used to export Spring beans for direct remoting from a Flex client.
 * 
 * <p>
 * By default, this FactoryBean will look for a BlazeDS config file at /WEB-INF/flex/services-config.xml. This location
 * may be overridden using the servicesConfigPath property. Spring's {@link ResourceLoader} abstraction is used to load
 * the config resources, so the location may be specified using ant-style paths.
 * 
 * <p>
 * The initialization of the MessageBroker logically consists of two phases:
 * <ol>
 * <li>
 * Parsing the BlazeDS XML configuration files and applying their settings to a newly created MessageBroker</li>
 * <li>
 * Starting the MessageBroker and its services</li>
 * </ol>
 * Further custom configuration of the MessageBroker can be achieved through registering custom
 * {@link MessageBrokerConfigProcessor} instances with this FactoryBean via the <code>configProcessors</code> property.
 * 
 * <p>
 * Http-based messages should be routed to the MessageBroker using the {@link DispatcherServlet} in combination with the
 * {@link MessageBrokerHandlerAdapter}.
 * </p>
 * 
 * @see MessageBrokerHandlerAdapter
 * @see MessageBrokerConfigProcessor
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerFactoryBean implements FactoryBean, BeanClassLoaderAware, BeanNameAware, ResourceLoaderAware, InitializingBean,
    DisposableBean, ServletContextAware {

    private static String FLEXDIR = "/WEB-INF/flex/";

    private static final Log logger = LogFactory.getLog(MessageBrokerFactoryBean.class);

    private MessageBroker messageBroker;

    private String name;

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    private ConfigurationManager configurationManager;

    private ResourceLoader resourceLoader;

    private String servicesConfigPath;

    private ServletContext servletContext;

    private Set<MessageBrokerConfigProcessor> configProcessors = new HashSet<MessageBrokerConfigProcessor>();

    /**
     * 
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {

        try {
            ServletConfig servletConfig = new DelegatingServletConfig();
    
            // Set the servlet config as thread local
            FlexContext.setThreadLocalObjects(null, null, null, null, null, servletConfig);
    
            // Get the configuration manager
            if (this.configurationManager == null) {
                this.configurationManager = new FlexConfigurationManager(this.resourceLoader, this.servicesConfigPath);
            }
    
            // Load configuration
            MessagingConfiguration messagingConfig = this.configurationManager.getMessagingConfiguration(servletConfig);
    
            // Set up logging system ahead of everything else.
            messagingConfig.createLogAndTargets();
    
            // Create broker.
            this.messageBroker = messagingConfig.createBroker(this.name, this.beanClassLoader);
    
            // Set the servlet config as thread local
            FlexContext.setThreadLocalObjects(null, null, this.messageBroker, null, null, servletConfig);
    
            setupInternalPathResolver();
    
            setInitServletContext();
    
            if (logger.isInfoEnabled()) {
                logger.info(VersionInfo.buildMessage());
            }
    
            // Create endpoints, services, security, and logger on the broker based
            // on configuration
            messagingConfig.configureBroker(this.messageBroker);
    
            long timeBeforeStartup = 0;
            if (logger.isInfoEnabled()) {
                timeBeforeStartup = System.currentTimeMillis();
                logger.info("MessageBroker with id '" + this.messageBroker.getId() + "' is starting.");
            }
    
            // initialize the httpSessionToFlexSessionMap
            synchronized (HttpFlexSession.mapLock) {
                if (servletConfig.getServletContext().getAttribute(HttpFlexSession.SESSION_MAP) == null) {
                    servletConfig.getServletContext().setAttribute(HttpFlexSession.SESSION_MAP, new ConcurrentHashMap());
                }
            }
    
            this.messageBroker = processBeforeStart(this.messageBroker);
    
            this.messageBroker.start();
    
            this.messageBroker = processAfterStart(this.messageBroker);
    
            if (logger.isInfoEnabled()) {
                long timeAfterStartup = System.currentTimeMillis();
                Long diffMillis = new Long(timeAfterStartup - timeBeforeStartup);
                logger.info("MessageBroker with id '" + this.messageBroker.getId() + "' is ready (startup time: '" + diffMillis + "' ms)");
            }
    
            // Report replaced tokens
            this.configurationManager.reportTokens();
    
            // Report any unused properties.
            messagingConfig.reportUnusedProperties();
        
        } catch (Throwable error) {
            //Ensure the broker gets cleaned up properly, then re-throw
            destroy();
            throw new BeanInitializationException("MessageBroker initialization failed", error);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void destroy() throws Exception {
        FlexContext.clearThreadLocalObjects();
        if (this.messageBroker != null) {
            this.messageBroker.stop();
            if (this.messageBroker.isManaged()) {
                MBeanLifecycleManager.unregisterRuntimeMBeans(this.messageBroker);
            }
        }
    }

    /**
     * Return the set of configuration processors that can customize the created {@link MessageBroker}
     * 
     * @return the config processors
     */
    public Set<MessageBrokerConfigProcessor> getConfigProcessors() {
        return this.configProcessors;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Object getObject() throws Exception {
        return this.messageBroker;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Class<? extends MessageBroker> getObjectType() {
        return this.messageBroker != null ? this.messageBroker.getClass() : MessageBroker.class;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * 
     * @param startupProcessors
     */
    public void setConfigProcessors(Set<MessageBrokerConfigProcessor> startupProcessors) {
        this.configProcessors = startupProcessors;
    }

    /**
     * 
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 
     * @param servicesConfigPath
     */
    public void setServicesConfigPath(String servicesConfigPath) {
        this.servicesConfigPath = servicesConfigPath;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    private MessageBroker processAfterStart(MessageBroker broker) {
        for (MessageBrokerConfigProcessor processor : this.configProcessors) {
            broker = processor.processAfterStartup(broker);
        }
        return broker;
    }

    private MessageBroker processBeforeStart(MessageBroker broker) {
        for (MessageBrokerConfigProcessor processor : this.configProcessors) {
            broker = processor.processBeforeStartup(broker);
        }
        return broker;
    }

    private void setInitServletContext() {

        // This is undesirable but necessary at the moment for LCDS to be able to load its license configuration.
        // Hopefully we can get the BlazeDS/LCDS team to give us a better option in the future.
        Method initMethod = ReflectionUtils.findMethod(MessageBroker.class, "setServletContext", new Class[] { ServletContext.class });
        if (initMethod == null) {
            initMethod = ReflectionUtils.findMethod(MessageBroker.class, "setInitServletContext", new Class[] { ServletContext.class });
        }
        ReflectionUtils.makeAccessible(initMethod);
        ReflectionUtils.invokeMethod(initMethod, this.messageBroker, new Object[] { this.servletContext });
    }

    private void setupInternalPathResolver() {
        this.messageBroker.setInternalPathResolver(new MessageBroker.InternalPathResolver() {

            public InputStream resolve(String filename) {

                try {
                    return MessageBrokerFactoryBean.this.resourceLoader.getResource(FLEXDIR + filename).getInputStream();
                } catch (IOException e) {
                    throw new IllegalStateException("Could not resolve Flex internal resource at: " + FLEXDIR + filename);
                }

            }
        });
    }

    /**
     * Internal implementation of the {@link ServletConfig} interface, to be passed to BlazeDS.
     */
    private class DelegatingServletConfig implements ServletConfig {

        public String getInitParameter(String paramName) {
            return null;
        }

        @SuppressWarnings("unchecked")
        public Enumeration getInitParameterNames() {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        public ServletContext getServletContext() {
            return MessageBrokerFactoryBean.this.servletContext;
        }

        public String getServletName() {
            return MessageBrokerFactoryBean.this.name;
        }
    }
}
