package org.springframework.flex.messaging;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.ServletConfigAware;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import flex.management.MBeanLifecycleManager;
import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.VersionInfo;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.MessagingConfiguration;

/**
 * 
 * TODO - Do we need to implement the "useContextClassLoader" option?
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerFactoryBean implements FactoryBean, BeanClassLoaderAware, BeanNameAware, ResourceLoaderAware,
		InitializingBean, DisposableBean, ServletConfigAware {

	private static String FLEXDIR = "/WEB-INF/flex/";

	private static final Log logger = LogFactory.getLog(MessageBrokerFactoryBean.class);

	private MessageBroker messageBroker;

	private String name;

	private ClassLoader beanClassLoader = getClass().getClassLoader();

	private ConfigurationManager configurationManager;

	private ResourceLoader resourceLoader;

	private ServletConfig servletConfig;

	private String servicesConfigPath;

	/**
	 * Return the singleton MessageBroker.
	 */
	public Object getObject() throws Exception {
		return this.messageBroker;
	}

	public Class getObjectType() {
		return (this.messageBroker != null ? this.messageBroker.getClass() : MessageBroker.class);
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

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setServicesConfigPath(String servicesConfigPath) {
		this.servicesConfigPath = servicesConfigPath;
	}

	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	public void afterPropertiesSet() throws Exception {
		
		// Set the servlet config as thread local
	        FlexContext.setThreadLocalObjects(null, null, null, null, null, servletConfig);
		
		// Get the configuration manager
		if (configurationManager == null) {
			configurationManager = new FlexConfigurationManager(resourceLoader, servicesConfigPath);
		}

		// Load configuration
		MessagingConfiguration config = configurationManager.getMessagingConfiguration(servletConfig);

		// Set up logging system ahead of everything else.
		config.createLogAndTargets();

		// Create broker.
		messageBroker = config.createBroker(name, beanClassLoader);
		
		// Set the servlet config as thread local
                FlexContext.setThreadLocalObjects(null, null, messageBroker, null, null, servletConfig);

	        setupInternalPathResolver();

		if (logger.isInfoEnabled()) {
			logger.info(VersionInfo.buildMessage());
		}

		// Create endpoints, services, security, and logger on the broker based on configuration
		config.configureBroker(messageBroker);

		long timeBeforeStartup = 0;
		if (logger.isInfoEnabled()) {
			timeBeforeStartup = System.currentTimeMillis();
			logger.info("MessageBroker with id '" + messageBroker.getId() + "' is starting.");
		}

		// initialize the httpSessionToFlexSessionMap
		synchronized (HttpFlexSession.mapLock) {
			if (servletConfig.getServletContext().getAttribute(HttpFlexSession.SESSION_MAP) == null)
				servletConfig.getServletContext().setAttribute(HttpFlexSession.SESSION_MAP,
						new ConcurrentHashMap());
		}

		messageBroker.start();
		
		if (logger.isInfoEnabled()) {
			long timeAfterStartup = System.currentTimeMillis();
			Long diffMillis = new Long(timeAfterStartup - timeBeforeStartup);
			logger.info("MessageBroker with id '" + messageBroker.getId() + "' is ready (startup time: '"
					+ diffMillis + "' ms)");
		}
		
		// Report replaced tokens
	        configurationManager.reportTokens();

	        // Report any unused properties.
	        config.reportUnusedProperties();
	}

	
	public void destroy() throws Exception {
		FlexContext.clearThreadLocalObjects();
		if (messageBroker != null)
	        {
	            messageBroker.stop();
	            if (messageBroker.isManaged())
	            {
	                MBeanLifecycleManager.unregisterRuntimeMBeans(messageBroker);
	            }
	        }
	}
	

	private void setupInternalPathResolver() {
		messageBroker.setInternalPathResolver(new MessageBroker.InternalPathResolver() {
			public InputStream resolve(String filename) {

				try {
					return resourceLoader.getResource(FLEXDIR + filename).getInputStream();
				} catch (IOException e) {
					throw new IllegalStateException("Could not resolve Flex internal resource at: "
							+ FLEXDIR + filename);
				}

			}
		});
	}

}
