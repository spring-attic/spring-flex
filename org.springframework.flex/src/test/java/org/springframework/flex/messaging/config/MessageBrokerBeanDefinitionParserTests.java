package org.springframework.flex.messaging.config;

import javax.servlet.ServletConfig;

import junit.framework.TestCase;

import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.flex.messaging.servlet.MessageBrokerHandlerAdapter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import flex.messaging.MessageBroker;
import flex.messaging.config.MessagingConfiguration;

public class MessageBrokerBeanDefinitionParserTests extends TestCase {

	private static final XmlWebApplicationContext context;
	private static final MockServletContext servletContext;
	
	static {
		context = new XmlWebApplicationContext();
		context.setConfigLocation("classpath:org/springframework/flex/messaging/config/message-broker.xml");
		servletContext = new MockServletContext(new WebInfResourceLoader());
		context.setServletContext(servletContext);
		context.refresh();
	}
	
	private MessageBroker broker;
	
	public void setUp() throws Exception {
		
	}
	
	public void testMessageBrokerDefaultConfig() {
		broker = (MessageBroker) context.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		assertTrue("MessageBroker should be started",broker.isStarted());
		assertNotNull("MessageBrokerHandlerAdapter not found", context.getBean(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER, MessageBrokerHandlerAdapter.class));
		SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) context.getBean(BeanIds.MESSAGE_BROKER+"DefaultHandlerMapping", SimpleUrlHandlerMapping.class);
		assertTrue("Default mapping not correct", defaultMapping.getUrlMap().containsKey("/*"));
		assertEquals("Default mapping not correct", BeanIds.MESSAGE_BROKER, defaultMapping.getUrlMap().get("/*"));
	}
	
	public void testMessageBroker_CustomConfigManager() {
		broker = (MessageBroker) context.getBean("customConfigManager", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		TestConfigurationManager configMgr = (TestConfigurationManager) context.getBean("configManager", TestConfigurationManager.class);
		assertNotNull("Custom ConfigurationManager not found");
		assertTrue("The custom ConfigurationManager was not used",configMgr.invoked);
		
	}
	
	public void testMessageBroker_CustomServicesConfigPath() {
		broker = (MessageBroker) context.getBean("customServicesConfigPath", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		assertTrue("Custom configuration was not read",broker.getChannelIds().contains("my-custom-path-channel"));
	}
	
	public void testMessageBroker_InvalidConfig() {
		try {
			new ClassPathXmlApplicationContext("org/springframework/flex/messaging/config/invalid-message-broker.xml");
			fail("Invalid message-broker config was not caught");
		} catch (BeanDefinitionParsingException ex) {
			//Expected
		}
	}
	
	public void testMessageBroker_CustomMappings() {
		broker = (MessageBroker) context.getBean("customMappings", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) context.getBean("customMappingsDefaultHandlerMapping", SimpleUrlHandlerMapping.class);
		assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/foo"));
		assertEquals("Target mapping not correct", "customMappingsDefaultHandlerMapping", defaultMapping.getUrlMap().get("/foo"));
		assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/bar"));
		assertEquals("Target mapping not correct", "customMappingsDefaultHandlerMapping", defaultMapping.getUrlMap().get("/bar"));
	}
	
	public void testMessageBroker_DisabledHandlerMapping() {
		broker = (MessageBroker) context.getBean("disabledHandlerMapping", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		assertFalse("Default handler mapping not disabled",context.containsBean("disabledHandlerMappingDefaultHandlerMapping"));
	}
	
	private static final class WebInfResourceLoader implements ResourceLoader {

		public ClassLoader getClassLoader() {
			return context.getClassLoader();
		}

		public Resource getResource(String location) {
			if (location.startsWith("/WEB-INF/flex/"))
			{
				location = location.replace("/WEB-INF/flex/", "classpath:org/springframework/flex/messaging/");
			}
			return context.getResource(location);
		}
		
	}
	
	public static final class TestConfigurationManager extends FlexConfigurationManager{
		
		protected boolean invoked = false;

		@Override
		public MessagingConfiguration getMessagingConfiguration(
				ServletConfig servletConfig) {
			invoked=true;
			return super.getMessagingConfiguration(servletConfig);
		}
	}
}
