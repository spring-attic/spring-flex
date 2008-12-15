package org.springframework.flex.messaging.config;

import javax.servlet.ServletConfig;

import junit.framework.TestCase;

import org.springframework.flex.messaging.config.FlexConfigurationManager;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.context.support.StaticWebApplicationContext;

import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.MessagingConfiguration;

public class FlexConfigurationManagerTests extends TestCase {

	ServletConfig config = new MockServletConfig();
	StaticWebApplicationContext loader = new StaticWebApplicationContext();
	
	ConfigurationManager configManager;
	
	public void setUp() {
		loader.setServletConfig(config);
	}
	
	public void testGetMessagingConfiguration() {
		configManager = new FlexConfigurationManager(loader, "classpath:org/springframework/flex/messaging/services-config.xml");
		
		MessagingConfiguration messagingConfiguration = configManager.getMessagingConfiguration(config);
		
		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
	}
	
	public void testGetMessagingConfiguration_NullServletConfig() {
		configManager = new FlexConfigurationManager(loader, "classpath:org/springframework/flex/messaging/services-config.xml");
		
		try {
			configManager.getMessagingConfiguration(null);
			fail();
		} catch (Exception ex) {
			//expected
		}	
	}
}
