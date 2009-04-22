package org.springframework.flex.config.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.core.ExceptionTranslationAdvice;
import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.security.EndpointInterceptor;
import org.springframework.flex.security.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.flex.security.SpringSecurityLoginCommand;
import org.springframework.flex.servlet.MessageBrokerHandlerAdapter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.messages.Message;
import flex.messaging.security.LoginCommand;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class MessageBrokerBeanDefinitionParserTests extends AbstractFlexConfigurationTests {
	
	private MessageBroker broker;

	public void testMessageBrokerDefaultConfig() {
		broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
		assertNotNull("MessageBroker bean not found for default ID", broker);
		assertTrue("MessageBroker should be started",broker.isStarted());
		assertNotNull("MessageBroker should have a RemotingService", broker.getServiceByType(RemotingService.class.getName()));
		assertNotNull("MessageBrokerHandlerAdapter not found", getApplicationContext().getBean(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER, MessageBrokerHandlerAdapter.class));
		SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER+"DefaultHandlerMapping", SimpleUrlHandlerMapping.class);
		assertTrue("Default mapping not correct", defaultMapping.getUrlMap().containsKey("/*"));
		assertEquals("Default mapping not correct", BeanIds.MESSAGE_BROKER, defaultMapping.getUrlMap().get("/*"));
	}
	
	public void testMessageBroker_CustomConfigManager() {
		broker = (MessageBroker) getApplicationContext().getBean("customConfigManager", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		TestConfigurationManager configMgr = (TestConfigurationManager) getApplicationContext().getBean("configManager", TestConfigurationManager.class);
		assertNotNull("Custom ConfigurationManager not found");
		assertTrue("The custom ConfigurationManager was not used",configMgr.invoked);
		
	}
	
	public void testMessageBroker_CustomServicesConfigPath() {
		broker = (MessageBroker) getApplicationContext().getBean("customServicesConfigPath", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		assertTrue("Custom configuration was not read",broker.getChannelIds().contains("my-custom-path-channel"));
	}
	
	public void testMessageBroker_InvalidConfig() {
		try {
			new ClassPathXmlApplicationContext("org/springframework/flex/config/invalid-message-broker.xml");
			fail("Invalid message-broker config was not caught");
		} catch (BeanDefinitionParsingException ex) {
			//Expected
		}
	}
	
	public void testMessageBroker_CustomMappings() {
		broker = (MessageBroker) getApplicationContext().getBean("customMappings", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) getApplicationContext().getBean("customMappingsDefaultHandlerMapping", SimpleUrlHandlerMapping.class);
		assertEquals(0,defaultMapping.getOrder());
		assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/foo"));
		assertEquals("Target mapping not correct", "customMappings", defaultMapping.getUrlMap().get("/foo"));
		assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/bar"));
		assertEquals("Target mapping not correct", "customMappings", defaultMapping.getUrlMap().get("/bar"));
	}
	
	public void testMessageBroker_DisabledHandlerMapping() {
		broker = (MessageBroker) getApplicationContext().getBean("disabledHandlerMapping", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		assertFalse("Default handler mapping not disabled",getApplicationContext().containsBean("disabledHandlerMappingDefaultHandlerMapping"));
	}
	
	public void testMessageBroker_CustomConfigProcessor() {
		broker = (MessageBroker) getApplicationContext().getBean("customConfigProcessors", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		TestConfigProcessor processor1 = (TestConfigProcessor) getApplicationContext().getBean("processor1", TestConfigProcessor.class);
		TestConfigProcessor processor2 = (TestConfigProcessor) getApplicationContext().getBean("processor2", TestConfigProcessor.class);
		assertTrue("Processor1 not invoked", processor1.beforeProcessed && processor1.afterProcessed);
		assertTrue("Processor2 not invoked", processor2.beforeProcessed && processor2.afterProcessed);
	}
	
	@SuppressWarnings("unchecked")
	public void testMessageBroker_CustomExceptionTranslator() {
		broker = (MessageBroker) getApplicationContext().getBean("customExceptionTranslators", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		Iterator i = broker.getEndpoints().values().iterator();
		while (i.hasNext()) {
			Object endpoint = i.next();
			assertTrue("Endpoint should be proxied",AopUtils.isAopProxy(endpoint));
			Advised advisedEndpoint = (Advised) endpoint;
			Advisor a = advisedEndpoint.getAdvisors()[0];
			assertTrue("Exception translation advice was not applied",a.getAdvice() instanceof ExceptionTranslationAdvice);
			Set translators = ((ExceptionTranslationAdvice)a.getAdvice()).getExceptionTranslators();
			assertTrue("Custom translator not found", translators.contains(getApplicationContext().getBean("translator1", TestExceptionTranslator.class)));
			assertTrue("Custom translator not found", translators.contains(getApplicationContext().getBean("translator2", TestExceptionTranslator.class)));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testMessageBroker_CustomMessageInterceptors() {
		broker = (MessageBroker) getApplicationContext().getBean("customMessageInterceptors", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		Iterator i = broker.getEndpoints().values().iterator();
		while (i.hasNext()) {
			Object endpoint = i.next();
			assertTrue("Endpoint should be proxied",AopUtils.isAopProxy(endpoint));
			Advised advisedEndpoint = (Advised) endpoint;
			Advisor a = advisedEndpoint.getAdvisors()[1];
			assertTrue("Message interception advice was not applied",a.getAdvice() instanceof MessageInterceptionAdvice);
			Set interceptors = ((MessageInterceptionAdvice)a.getAdvice()).getMessageInterceptors();
			assertTrue("Custom interceptor not found", interceptors.contains(getApplicationContext().getBean("interceptor1", TestMessageInterceptor.class)));
			assertTrue("Custom interceptor not found", interceptors.contains(getApplicationContext().getBean("interceptor2", TestMessageInterceptor.class)));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testMessageBroker_DefaultSecured() {
		broker = (MessageBroker) getApplicationContext().getBean("defaultSecured", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		LoginCommand loginCommand = broker.getLoginManager().getLoginCommand();
		assertNotNull("LoginCommand not found", loginCommand);
		assertTrue("LoginCommand of wrong type", loginCommand instanceof SpringSecurityLoginCommand);
		assertSame("LoginCommand not a managed spring bean", loginCommand, getApplicationContext().getBean("defaultSecuredLoginCommand"));
		Iterator i = broker.getEndpoints().values().iterator();
		while (i.hasNext()) {
			Object endpoint = i.next();
			assertTrue("Endpoint should be proxied",AopUtils.isAopProxy(endpoint));
			Advised advisedEndpoint = (Advised) endpoint;
			Advisor a = advisedEndpoint.getAdvisors()[0];
			assertTrue("Exception translation advice was not applied",a.getAdvice() instanceof ExceptionTranslationAdvice);
			a = advisedEndpoint.getAdvisors()[1];
			assertTrue("Message interception advice was not applied",a.getAdvice() instanceof MessageInterceptionAdvice);
		}
		getApplicationContext().getBean(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, FlexSessionInvalidatingAuthenticationListener.class);
		getApplicationContext().getBean(BeanIds.REQUEST_CONTEXT_FILTER, RequestContextFilter.class);
	}
	
	@SuppressWarnings("unchecked")
	public void testMessageBroker_LoginCommandConfigured() {
		broker = (MessageBroker) getApplicationContext().getBean("loginCommandConfigured", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		SpringSecurityLoginCommand loginCommand = (SpringSecurityLoginCommand) broker.getLoginManager().getLoginCommand();
		assertNotNull("LoginCommand not found", loginCommand);
		assertTrue("perClientAuthentication not configured",loginCommand.isPerClientAuthentication());
		
		Iterator i = broker.getEndpoints().values().iterator();
		while (i.hasNext()) {
			Object endpoint = i.next();
			assertTrue("Endpoint should be proxied",AopUtils.isAopProxy(endpoint));
			Advised advisedEndpoint = (Advised) endpoint;
			Advisor a = advisedEndpoint.getAdvisors()[1];
			assertTrue("Message interception advice was not applied",a.getAdvice() instanceof MessageInterceptionAdvice);			
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testMessageBroker_EndpointSecured() {
		broker = (MessageBroker) getApplicationContext().getBean("endpointSecured", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		Iterator i = broker.getEndpoints().values().iterator();
		while (i.hasNext()) {
			Object endpoint = i.next();
			assertTrue("Endpoint should be proxied",AopUtils.isAopProxy(endpoint));
			Advised advisedEndpoint = (Advised) endpoint;
			Advisor a = advisedEndpoint.getAdvisors()[1];
			assertTrue("MessageInterception advice was not applied",a.getAdvice() instanceof MessageInterceptionAdvice);
			Iterator<MessageInterceptor> m = ((MessageInterceptionAdvice)a.getAdvice()).getMessageInterceptors().iterator();
			while(m.hasNext()){
				MessageInterceptor interceptor = m.next();
				if (interceptor instanceof EndpointInterceptor) {
					Collection definitions = ((EndpointInterceptor) interceptor).getObjectDefinitionSource().getConfigAttributeDefinitions();
					assertEquals("Incorrect number of EnpointDefinitionSource instances", 3, definitions.size());
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testMessageBroker_CustomRemotingService() {
		broker = (MessageBroker) getApplicationContext().getBean("customRemotingService", MessageBroker.class);
		assertNotNull("MessageBroker bean not found for custom id", broker);
		RemotingService remotingService = (RemotingService) broker.getServiceByType(RemotingService.class.getName());
		assertNotNull("RemotingService not found", remotingService);
		String defaultAdapterId = remotingService.getDefaultAdapter();
		assertEquals("Default adapter id not set on RemotingService", "my-default-adapter", defaultAdapterId);
		List expectedChannels = new ArrayList();
		expectedChannels.add("my-amf");
		expectedChannels.add("my-secure-amf");
		assertEquals("Default channels not set", expectedChannels, remotingService.getDefaultChannels());
		
		TestJavaAdapter adapter = (TestJavaAdapter) getApplicationContext().getBean(defaultAdapterId, TestJavaAdapter.class);
		assertTrue(adapter.initialized);
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
	
	public static final class TestConfigProcessor implements MessageBrokerConfigProcessor {

		protected boolean afterProcessed = false;
		protected boolean beforeProcessed = false;
		
		public MessageBroker processAfterStartup(MessageBroker broker) {
			afterProcessed = true;
			return broker;
		}

		public MessageBroker processBeforeStartup(MessageBroker broker) {
			beforeProcessed = true;
			return broker;
		}
		
	}
	
	public static final class TestExceptionTranslator implements ExceptionTranslator {
		public boolean handles(Class<?> clazz) {
			return false;
		}

		public MessageException translate(Throwable t) {
			return null;
		}
	}
	
	public static final class TestMessageInterceptor implements MessageInterceptor {
		public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
			return null;
		}

		public Message preProcess(MessageProcessingContext context, Message inputMessage) {
			return null;
		}
	}
	
	public static final class TestJavaAdapter extends JavaAdapter {

		protected boolean initialized = false;
		
		@Override
		public void initialize(String id, ConfigMap properties) {
			ConfigMap foo = properties.getPropertyAsMap("foo", null);
			assertNotNull(foo);
			assertTrue(foo.getPropertyAsBoolean("bar", false));
			assertEquals("moo", foo.getProperty("baz"));
			initialized = true;
		}
	}
}
