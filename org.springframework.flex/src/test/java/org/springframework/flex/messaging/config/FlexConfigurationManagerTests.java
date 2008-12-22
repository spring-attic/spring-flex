package org.springframework.flex.messaging.config;

import javax.servlet.ServletConfig;

import junit.framework.TestCase;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.flex.messaging.config.FlexConfigurationManager;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.context.support.StaticWebApplicationContext;

import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.MessagingConfiguration;

public class FlexConfigurationManagerTests extends TestCase {

	ServletConfig config = new MockServletConfig();
	StaticWebApplicationContext context = new StaticWebApplicationContext();
	
	ConfigurationManager configManager;
	
	public void setUp() {
		context.setServletConfig(config);
	}
	
	public void testGetMessagingConfiguration() {
		configManager = new FlexConfigurationManager(context, "classpath:org/springframework/flex/messaging/services-config.xml");
		
		MessagingConfiguration messagingConfiguration = configManager.getMessagingConfiguration(config);
		
		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
	}
	
	public void testGetMessagingConfiguration_NullServletConfig() {
		configManager = new FlexConfigurationManager(context, "classpath:org/springframework/flex/messaging/services-config.xml");
		
		try {
			configManager.getMessagingConfiguration(null);
			fail();
		} catch (Exception ex) {
			//expected
		}	
	}
	
	public void testCustomConfiguration() {
		context.registerSingleton("configParser", flex.messaging.config.XPathServerConfigurationParser.class);
		RuntimeBeanReference parserReference = new RuntimeBeanReference("configParser");
		GenericBeanDefinition configManagerDef = new GenericBeanDefinition();
		configManagerDef.setBeanClass(FlexConfigurationManager.class);
		configManagerDef.getPropertyValues().addPropertyValue("configurationParser", parserReference);
		configManagerDef.getPropertyValues().addPropertyValue("configurationPath", "classpath:org/springframework/flex/messaging/services-config.xml");
		context.getDefaultListableBeanFactory().registerBeanDefinition("configurationManager", configManagerDef);
		context.refresh();
		
		configManager = (ConfigurationManager) context.getBean("configurationManager");
		
		MessagingConfiguration messagingConfiguration = configManager.getMessagingConfiguration(config);
		
		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
	}
}
