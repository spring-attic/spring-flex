package org.springframework.flex.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.flex.messaging.config.FlexConfigurationManager;
import org.springframework.flex.messaging.config.MessageBrokerConfigProcessor;
import org.springframework.flex.messaging.servlet.MessageBrokerHandlerAdapter;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.DispatcherServlet;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import flex.management.MBeanLifecycleManager;
import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.VersionInfo;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.MessagingConfiguration;

/**
 * {@link FactoryBean} that creates a local {@link MessageBroker} instance
 * within a Spring web application context. The resulting Spring-managed
 * MessageBroker can be used to export Spring beans for direct remoting from a
 * Flex client.
 * 
 * <p>
 * By default, this FactoryBean will look for a BlazeDS config file at
 * /WEB-INF/flex/services-config.xml. This location may be overridden using the
 * servicesConfigPath property. Spring's {@link ResourceLoader} abstraction is
 * used to load the config resources, so the location may be specified using
 * ant-style paths.
 * </p>
 * 
 * <p>
 * Http-based messages should be routed to the MessageBroker using the
 * {@link DispatcherServlet} in combination with the
 * {@link MessageBrokerHandlerAdapter}.
 * </p>
 * 
 * @see MessageBrokerHandlerAdapter
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerFactoryBean implements FactoryBean,
		BeanClassLoaderAware, BeanNameAware, ResourceLoaderAware,
		InitializingBean, DisposableBean, ServletContextAware {

	private static String FLEXDIR = "/WEB-INF/flex/";

	private static final Log logger = LogFactory
			.getLog(MessageBrokerFactoryBean.class);

	private MessageBroker messageBroker;

	private String name;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private ConfigurationManager configurationManager;

	private ResourceLoader resourceLoader;

	private String servicesConfigPath;

	private ServletContext servletContext;

	private Set<MessageBrokerConfigProcessor> configProcessors = new HashSet<MessageBrokerConfigProcessor>();

	/**
	 * Return the singleton MessageBroker.
	 */
	public Object getObject() throws Exception {
		return this.messageBroker;
	}

	public Class<? extends MessageBroker> getObjectType() {
		return (this.messageBroker != null ? this.messageBroker.getClass()
				: MessageBroker.class);
	}
	
	public Set<MessageBrokerConfigProcessor> getConfigProcessors() {
		return configProcessors;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	public void setBeanName(String name) {
		this.name = name;
	}

	public void setConfigurationManager(
			ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setServicesConfigPath(String servicesConfigPath) {
		this.servicesConfigPath = servicesConfigPath;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;		
	}

	public void setConfigProcessors(Set<MessageBrokerConfigProcessor> startupProcessors) {
		this.configProcessors = startupProcessors;
	}

	public void afterPropertiesSet() throws Exception {

		ServletConfig servletConfig = new DelegatingServletConfig();
		
		// Set the servlet config as thread local
		FlexContext.setThreadLocalObjects(null, null, null, null, null,
				servletConfig);

		// Get the configuration manager
		if (configurationManager == null) {
			configurationManager = new FlexConfigurationManager(resourceLoader,
					servicesConfigPath);
		}

		// Load configuration
		MessagingConfiguration messagingConfig = configurationManager
				.getMessagingConfiguration(servletConfig);

		// Set up logging system ahead of everything else.
		messagingConfig.createLogAndTargets();

		// Create broker.
		messageBroker = messagingConfig.createBroker(name, beanClassLoader);

		// Set the servlet config as thread local
		FlexContext.setThreadLocalObjects(null, null, messageBroker, null,
				null, servletConfig);

		setupInternalPathResolver();

		if (logger.isInfoEnabled()) {
			logger.info(VersionInfo.buildMessage());
		}

		// Create endpoints, services, security, and logger on the broker based
		// on configuration
		messagingConfig.configureBroker(messageBroker);

		long timeBeforeStartup = 0;
		if (logger.isInfoEnabled()) {
			timeBeforeStartup = System.currentTimeMillis();
			logger.info("MessageBroker with id '" + messageBroker.getId()
					+ "' is starting.");
		}

		// initialize the httpSessionToFlexSessionMap
		synchronized (HttpFlexSession.mapLock) {
			if (servletConfig.getServletContext().getAttribute(
					HttpFlexSession.SESSION_MAP) == null)
				servletConfig.getServletContext().setAttribute(
						HttpFlexSession.SESSION_MAP, new ConcurrentHashMap());
		}

		messageBroker = processBeforeStart(messageBroker);
		
		messageBroker.start();

		messageBroker = processAfterStart(messageBroker);
		
		if (logger.isInfoEnabled()) {
			long timeAfterStartup = System.currentTimeMillis();
			Long diffMillis = new Long(timeAfterStartup - timeBeforeStartup);
			logger.info("MessageBroker with id '" + messageBroker.getId()
					+ "' is ready (startup time: '" + diffMillis + "' ms)");
		}

		// Report replaced tokens
		configurationManager.reportTokens();

		// Report any unused properties.
		messagingConfig.reportUnusedProperties();
	}

	private MessageBroker processAfterStart(MessageBroker broker) {
		for(MessageBrokerConfigProcessor processor : configProcessors) {
			broker = processor.processAfterStartup(broker);
		}
		return broker;
	}

	private MessageBroker processBeforeStart(MessageBroker broker) {
		for(MessageBrokerConfigProcessor processor : configProcessors) {
			broker = processor.processBeforeStartup(broker);
		}
		return broker;
	}

	public void destroy() throws Exception {
		FlexContext.clearThreadLocalObjects();
		if (messageBroker != null) {
			messageBroker.stop();
			if (messageBroker.isManaged()) {
				MBeanLifecycleManager.unregisterRuntimeMBeans(messageBroker);
			}
		}
	}

	private void setupInternalPathResolver() {
		messageBroker
				.setInternalPathResolver(new MessageBroker.InternalPathResolver() {
					public InputStream resolve(String filename) {

						try {
							return resourceLoader.getResource(
									FLEXDIR + filename).getInputStream();
						} catch (IOException e) {
							throw new IllegalStateException(
									"Could not resolve Flex internal resource at: "
											+ FLEXDIR + filename);
						}

					}
				});
	}
	
	/**
	 * Internal implementation of the {@link ServletConfig} interface,
	 * to be passed to BlazeDS.
	 */
	private class DelegatingServletConfig implements ServletConfig {

		public String getServletName() {
			return name;
		}

		public ServletContext getServletContext() {
			return servletContext;
		}

		public String getInitParameter(String paramName) {
			return null;
		}

		@SuppressWarnings("unchecked")
		public Enumeration getInitParameterNames() {
			return Collections.enumeration(Collections.EMPTY_SET);
		}
	}
}
