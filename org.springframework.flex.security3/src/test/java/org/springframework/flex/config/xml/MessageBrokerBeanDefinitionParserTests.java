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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.config.TestWebInfResourceLoader;
import org.springframework.flex.core.ExceptionTranslationAdvice;
import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.flex.security3.EndpointInterceptor;
import org.springframework.flex.security3.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.flex.security3.SpringSecurityLoginCommand;
import org.springframework.flex.servlet.MessageBrokerHandlerAdapter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.messages.Message;
import flex.messaging.security.LoginCommand;
import flex.messaging.services.MessageService;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class MessageBrokerBeanDefinitionParserTests extends AbstractFlexConfigurationTests {

    private MessageBroker broker;

    
    @Override
    protected ConfigurableApplicationContext createParentContext() {
        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
        createBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/config/security-context.xml" });
        context.refresh();
        return context;
    }

    public void testMessageBroker_CustomConfigManager() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customConfigManager", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        TestConfigurationManager configMgr = (TestConfigurationManager) getApplicationContext().getBean("configManager",
            TestConfigurationManager.class);
        assertNotNull("Custom ConfigurationManager not found");
        assertTrue("The custom ConfigurationManager was not used", configMgr.invoked);

    }

    public void testMessageBroker_CustomConfigProcessor() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customConfigProcessors", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        TestConfigProcessor processor1 = (TestConfigProcessor) getApplicationContext().getBean("processor1", TestConfigProcessor.class);
        TestConfigProcessor processor2 = (TestConfigProcessor) getApplicationContext().getBean("processor2", TestConfigProcessor.class);
        assertTrue("Processor1 not invoked", processor1.beforeProcessed && processor1.afterProcessed);
        assertTrue("Processor2 not invoked", processor2.beforeProcessed && processor2.afterProcessed);
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_CustomExceptionTranslator() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customExceptionTranslators", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        Iterator i = this.broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[0];
            assertTrue("Exception translation advice was not applied", a.getAdvice() instanceof ExceptionTranslationAdvice);
            Set translators = ((ExceptionTranslationAdvice) a.getAdvice()).getExceptionTranslators();
            assertTrue("Custom translator not found", translators.contains(getApplicationContext().getBean("translator1",
                TestExceptionTranslator.class)));
            assertTrue("Custom translator not found", translators.contains(getApplicationContext().getBean("translator2",
                TestExceptionTranslator.class)));
        }
    }

    public void testMessageBroker_CustomMappings() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customMappings", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) getApplicationContext().getBean("customMappingsDefaultHandlerMapping",
            SimpleUrlHandlerMapping.class);
        assertEquals(0, defaultMapping.getOrder());
        assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/foo"));
        assertEquals("Target mapping not correct", "customMappings", defaultMapping.getUrlMap().get("/foo"));
        assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/bar"));
        assertEquals("Target mapping not correct", "customMappings", defaultMapping.getUrlMap().get("/bar"));
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_CustomMessageInterceptors() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customMessageInterceptors", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        Iterator i = this.broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
            Set interceptors = ((MessageInterceptionAdvice) a.getAdvice()).getMessageInterceptors();
            assertTrue("Custom interceptor not found", interceptors.contains(getApplicationContext().getBean("interceptor1",
                TestMessageInterceptor.class)));
            assertTrue("Custom interceptor not found", interceptors.contains(getApplicationContext().getBean("interceptor2",
                TestMessageInterceptor.class)));
        }
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_CustomMessageService() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customMessageService", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        MessageService messageService = (MessageService) this.broker.getServiceByType(MessageService.class.getName());
        assertNotNull("MessageService not found", messageService);
        String defaultAdapterId = messageService.getDefaultAdapter();
        assertEquals("Default adapter id not set on MessageService", "my-default-adapter", defaultAdapterId);
        List expectedChannels = new ArrayList();
        expectedChannels.add("my-polling-amf");
        expectedChannels.add("my-secure-amf");
        assertEquals("Default channels not set", expectedChannels, messageService.getDefaultChannels());
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_CustomRemotingService() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customRemotingService", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        RemotingService remotingService = (RemotingService) this.broker.getServiceByType(RemotingService.class.getName());
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

    public void testMessageBroker_CustomServicesConfigPath() {
        this.broker = (MessageBroker) getApplicationContext().getBean("customServicesConfigPath", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        assertTrue("Custom configuration was not read", this.broker.getChannelIds().contains("my-custom-path-channel"));
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_DefaultSecured() {
        this.broker = (MessageBroker) getApplicationContext().getBean("defaultSecured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        LoginCommand loginCommand = this.broker.getLoginManager().getLoginCommand();
        assertNotNull("LoginCommand not found", loginCommand);
        assertTrue("LoginCommand of wrong type", loginCommand instanceof SpringSecurityLoginCommand);
        assertSame("LoginCommand not a managed spring bean", loginCommand, getApplicationContext().getBean("defaultSecuredLoginCommand"));
        Iterator i = this.broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[0];
            assertTrue("Exception translation advice was not applied", a.getAdvice() instanceof ExceptionTranslationAdvice);
            a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
        }
        getApplicationContext().getBean(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, FlexSessionInvalidatingAuthenticationListener.class);
        RequestContextFilter filter = (RequestContextFilter) getApplicationContext().getBean(BeanIds.REQUEST_CONTEXT_FILTER, RequestContextFilter.class);
        FilterChainProxy filterChain = (FilterChainProxy) getApplicationContext().getParent().getBean("org.springframework.security.filterChainProxy", FilterChainProxy.class);
        assertTrue(((List)filterChain.getFilterChainMap().get("/**")).contains(filter));
    }

    public void testMessageBroker_DisabledHandlerMapping() {
        this.broker = (MessageBroker) getApplicationContext().getBean("disabledHandlerMapping", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        assertFalse("Default handler mapping not disabled", getApplicationContext().containsBean("disabledHandlerMappingDefaultHandlerMapping"));
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_EndpointSecured() {
        this.broker = (MessageBroker) getApplicationContext().getBean("endpointSecured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        Iterator i = this.broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[1];
            assertTrue("MessageInterception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
            Iterator<MessageInterceptor> m = ((MessageInterceptionAdvice) a.getAdvice()).getMessageInterceptors().iterator();
            while (m.hasNext()) {
                MessageInterceptor interceptor = m.next();
                if (interceptor instanceof EndpointInterceptor) {
                    Collection definitions = ((EndpointInterceptor) interceptor).getObjectDefinitionSource().getAllConfigAttributes();
                    assertEquals("Incorrect number of EnpointDefinitionSource instances", 3, definitions.size());
                }
            }
        }
    }

    public void testMessageBroker_InvalidConfig() {
        try {
            new ClassPathXmlApplicationContext("org/springframework/flex/config/invalid-message-broker.xml");
            fail("Invalid message-broker config was not caught");
        } catch (BeanDefinitionParsingException ex) {
            // Expected
        }
    }

    @SuppressWarnings("unchecked")
    public void testMessageBroker_LoginCommandConfigured() {
        this.broker = (MessageBroker) getApplicationContext().getBean("loginCommandConfigured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        SpringSecurityLoginCommand loginCommand = (SpringSecurityLoginCommand) this.broker.getLoginManager().getLoginCommand();
        assertNotNull("LoginCommand not found", loginCommand);
        assertTrue("perClientAuthentication not configured", loginCommand.isPerClientAuthentication());

        Iterator i = this.broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
        }
    }

    public void testMessageBrokerDefaultConfig() {
        this.broker = (MessageBroker) getApplicationContext().getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        assertTrue("MessageBroker should be started", this.broker.isStarted());
        assertNotNull("MessageBroker should have a RemotingService", this.broker.getServiceByType(RemotingService.class.getName()));
        assertNotNull("MessageBrokerHandlerAdapter not found", getApplicationContext().getBean(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER,
            MessageBrokerHandlerAdapter.class));
        SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) getApplicationContext().getBean(
            BeanIds.MESSAGE_BROKER + "DefaultHandlerMapping", SimpleUrlHandlerMapping.class);
        assertTrue("Default mapping not correct", defaultMapping.getUrlMap().containsKey("/*"));
        assertEquals("Default mapping not correct", BeanIds.MESSAGE_BROKER, defaultMapping.getUrlMap().get("/*"));
    }

    public static final class TestConfigProcessor implements MessageBrokerConfigProcessor {

        protected boolean afterProcessed = false;

        protected boolean beforeProcessed = false;

        public MessageBroker processAfterStartup(MessageBroker broker) {
            this.afterProcessed = true;
            return broker;
        }

        public MessageBroker processBeforeStartup(MessageBroker broker) {
            this.beforeProcessed = true;
            return broker;
        }

    }

    public static final class TestConfigurationManager extends FlexConfigurationManager {

        protected boolean invoked = false;

        @Override
        public MessagingConfiguration getMessagingConfiguration(ServletConfig servletConfig) {
            this.invoked = true;
            return super.getMessagingConfiguration(servletConfig);
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

    public static final class TestJavaAdapter extends JavaAdapter {

        protected boolean initialized = false;

        @Override
        public void initialize(String id, ConfigMap properties) {
            ConfigMap foo = properties.getPropertyAsMap("foo", null);
            assertNotNull(foo);
            assertTrue(foo.getPropertyAsBoolean("bar", false));
            assertEquals("moo", foo.getProperty("baz"));
            this.initialized = true;
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
    
    public static final class AccessDecisionManagerLogger implements BeanFactoryAware, InitializingBean {
        
        DefaultListableBeanFactory beanFactory;
        
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = (DefaultListableBeanFactory) beanFactory;            
        }

        public void afterPropertiesSet() throws Exception {
            System.out.println(StringUtils.arrayToCommaDelimitedString(beanFactory.getBeanNamesForType(AccessDecisionManager.class)));            
        }
        
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "classpath:org/springframework/flex/config/message-broker.xml" };
    }
}
