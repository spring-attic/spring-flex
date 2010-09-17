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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.config.MessageBrokerContextLoader;
import org.springframework.flex.config.TestWebInfResourceLoader;
import org.springframework.flex.core.ExceptionTranslationAdvice;
import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.flex.core.ResourceHandlingMessageInterceptor;
import org.springframework.flex.core.io.Person;
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.security3.EndpointInterceptor;
import org.springframework.flex.security3.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.flex.security3.SpringSecurityLoginCommand;
import org.springframework.flex.servlet.MessageBrokerHandlerAdapter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.messages.Message;
import flex.messaging.security.LoginCommand;
import flex.messaging.services.MessageService;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.adapters.JavaAdapter;

@ContextConfiguration(locations="classpath:org/springframework/flex/config/message-broker.xml", loader=MessageBrokerBeanDefinitionParserTests.ParentContextLoader.class)
public class MessageBrokerBeanDefinitionParserTests extends AbstractFlexConfigurationTests {

    private MessageBroker broker;

    public void testMessageBroker_CustomConfigManager() {
        this.broker = applicationContext.getBean("customConfigManager", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        TestConfigurationManager configMgr = (TestConfigurationManager) applicationContext.getBean("configManager",
            TestConfigurationManager.class);
        assertNotNull("Custom ConfigurationManager not found");
        assertTrue("The custom ConfigurationManager was not used", configMgr.invoked);

    }

    public void testMessageBroker_CustomConfigProcessor() {
        this.broker = applicationContext.getBean("customConfigProcessors", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        TestConfigProcessor processor1 = (TestConfigProcessor) applicationContext.getBean("processor1", TestConfigProcessor.class);
        TestConfigProcessor processor2 = (TestConfigProcessor) applicationContext.getBean("processor2", TestConfigProcessor.class);
        assertTrue("Processor1 not invoked", processor1.beforeProcessed && processor1.afterProcessed);
        assertTrue("Processor2 not invoked", processor2.beforeProcessed && processor2.afterProcessed);
        
    }

    @IfProfileValue(name=ENVIRONMENT, value=LCDS)
    public void testMessageBroker_CustomConfigProcessor_DataServices() {
        this.broker = applicationContext.getBean("customConfigProcessors", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);

        try {
            // This beans should not be found in application context.
            applicationContext.getBean("customConfigProcessors" + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX, 
                Class.forName(MessageBrokerBeanDefinitionParser.DATASERVICES_PROCESSOR_CLASS_NAME));
        } catch (NoSuchBeanDefinitionException e) {
            return;            
        } catch (Exception e) {
            fail("Unexpected exception:" + e);
        }

        fail("No exception was thrown. Excepted 'org.springframework.beans.factory.NoSuchBeanDefinitionException' to be thrown");
    }

    public void testMessageBroker_HibernateAutoConfigured() {
        assertTrue(PropertyProxyRegistry.getRegistry().getProxy(Person.class) instanceof SpringPropertyProxy);
    }

    public void testMessageBroker_CustomExceptionTranslator() {
        this.broker = applicationContext.getBean("customExceptionTranslators", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        for (Endpoint endpoint : this.broker.getEndpoints().values()) {
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[0];
            assertTrue("Exception translation advice was not applied", a.getAdvice() instanceof ExceptionTranslationAdvice);
            Set<ExceptionTranslator> translators = ((ExceptionTranslationAdvice) a.getAdvice()).getExceptionTranslators();
            assertTrue("Custom translator not found", translators.contains(applicationContext.getBean("translator1",
                TestExceptionTranslator.class)));
            assertTrue("Custom translator not found", translators.contains(applicationContext.getBean("translator2",
                TestExceptionTranslator.class)));
        }
    }
    
    @IfProfileValue(name=ENVIRONMENT, value=LCDS)
    public void testMessageBroker_CustomExceptionTranslator_DataServices() {
        this.broker = applicationContext.getBean("customExceptionTranslators", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);

        try {
            Set<ExceptionTranslator> translators = getExceptionTranslators(getDataServicesConfigProcessor("customExceptionTranslators"));
            assertEquals(2, translators.size());
            assertTrue("Custom translator not found", translators.contains(applicationContext.getBean("translator1",
                TestExceptionTranslator.class)));
            assertTrue("Custom translator not found", translators.contains(applicationContext.getBean("translator2",
                TestExceptionTranslator.class)));
            return;
            
        } catch(NoSuchBeanDefinitionException e) {
            fail("Expected " + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX + "to be registered");
        } catch (Exception e) {
            fail("Unexpected exception:" + e);
        }
    }
    
    public void testMessageBroker_CustomMappings() {
        this.broker = applicationContext.getBean("customMappings", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) applicationContext.getBean("customMappingsDefaultHandlerMapping",
            SimpleUrlHandlerMapping.class);
        assertEquals(0, defaultMapping.getOrder());
        assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/foo"));
        assertEquals("Target mapping not correct", "customMappings", defaultMapping.getUrlMap().get("/foo"));
        assertTrue("Path mapping not correct", defaultMapping.getUrlMap().containsKey("/bar"));
        assertEquals("Target mapping not correct", "customMappings", defaultMapping.getUrlMap().get("/bar"));
    }

    public void testMessageBroker_CustomMessageInterceptors() {
        this.broker = applicationContext.getBean("customMessageInterceptors", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        for (Endpoint endpoint : this.broker.getEndpoints().values()) {
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
            Set<MessageInterceptor> interceptors = ((MessageInterceptionAdvice) a.getAdvice()).getMessageInterceptors();
            assertTrue("Custom interceptor not found", interceptors.contains(applicationContext.getBean("interceptor1",
                TestMessageInterceptor.class)));
            assertTrue("Custom interceptor not found", interceptors.contains(applicationContext.getBean("interceptor2",
                TestMessageInterceptor.class)));
            assertTrue("Custom interceptor not found", interceptors.contains(applicationContext.getBean("interceptor3",
                TestResourceHandlingInterceptor.class)));
        }
    }
    
    @IfProfileValue(name=ENVIRONMENT, value=LCDS)
    public void testMessageBroker_CustomMessageInterceptors_DataServices() {
        this.broker = applicationContext.getBean("customMessageInterceptors", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);

        try {
            Set<MessageInterceptor> interceptors = getMessageInterceptors(getDataServicesConfigProcessor("customMessageInterceptors"));
            assertEquals(3, interceptors.size());
            assertTrue("Custom interceptor not found", interceptors.contains(applicationContext.getBean("interceptor1",
                TestMessageInterceptor.class)));
            assertTrue("Custom interceptor not found", interceptors.contains(applicationContext.getBean("interceptor2",
                TestMessageInterceptor.class)));
            assertTrue("Custom interceptor not found", interceptors.contains(applicationContext.getBean("interceptor3",
                TestResourceHandlingInterceptor.class)));
            return;
        } catch(NoSuchBeanDefinitionException e) {
            fail("Expected " + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX + "to be registered");
        } catch (Exception e) {
            fail("Unexpected exception:" + e);
        }
    }

    public void testMessageBroker_CustomMessageService() {
        this.broker = applicationContext.getBean("customMessageService", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        MessageService messageService = (MessageService) this.broker.getServiceByType(MessageService.class.getName());
        assertNotNull("MessageService not found", messageService);
        String defaultAdapterId = messageService.getDefaultAdapter();
        assertEquals("Default adapter id not set on MessageService", "my-default-adapter", defaultAdapterId);
        List<String> expectedChannels = new ArrayList<String>();
        expectedChannels.add("my-polling-amf");
        expectedChannels.add("my-secure-amf");
        assertEquals("Default channels not set", expectedChannels, messageService.getDefaultChannels());
    }

    public void testMessageBroker_CustomRemotingService() {
        this.broker = applicationContext.getBean("customRemotingService", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        RemotingService remotingService = (RemotingService) this.broker.getServiceByType(RemotingService.class.getName());
        assertNotNull("RemotingService not found", remotingService);
        String defaultAdapterId = remotingService.getDefaultAdapter();
        assertEquals("Default adapter id not set on RemotingService", "my-default-adapter", defaultAdapterId);
        List<String> expectedChannels = new ArrayList<String>();
        expectedChannels.add("my-amf");
        expectedChannels.add("my-secure-amf");
        assertEquals("Default channels not set", expectedChannels, remotingService.getDefaultChannels());

        TestJavaAdapter adapter = (TestJavaAdapter) applicationContext.getBean(defaultAdapterId, TestJavaAdapter.class);
        assertTrue(adapter.initialized);
    }

    public void testMessageBroker_CustomServicesConfigPath() {
        this.broker = applicationContext.getBean("customServicesConfigPath", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        assertTrue("Custom configuration was not read", this.broker.getChannelIds().contains("my-custom-path-channel"));
    }

    public void testMessageBroker_DefaultSecured() {
        this.broker = applicationContext.getBean("defaultSecured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        LoginCommand loginCommand = this.broker.getLoginManager().getLoginCommand();
        assertNotNull("LoginCommand not found", loginCommand);
        assertTrue("LoginCommand of wrong type", loginCommand instanceof SpringSecurityLoginCommand);
        assertSame("LoginCommand not a managed spring bean", loginCommand, applicationContext.getBean("defaultSecuredLoginCommand"));
        for (Endpoint endpoint : this.broker.getEndpoints().values()) {
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[0];
            assertTrue("Exception translation advice was not applied", a.getAdvice() instanceof ExceptionTranslationAdvice);
            a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
        }
        applicationContext.getBean(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, FlexSessionInvalidatingAuthenticationListener.class);
        RequestContextFilter filter = (RequestContextFilter) applicationContext.getBean(BeanIds.REQUEST_CONTEXT_FILTER, RequestContextFilter.class);
        FilterChainProxy filterChain = (FilterChainProxy) applicationContext.getParent().getBean("org.springframework.security.filterChainProxy", FilterChainProxy.class);
        assertTrue((filterChain.getFilterChainMap().get("/**")).contains(filter));
    }
    
    @IfProfileValue(name=ENVIRONMENT, value=LCDS)
    public void testMessageBroker_DefaultSecured_DataServices() {
        this.broker = applicationContext.getBean("defaultSecured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);

        try {
            Object dataServicesConfigProcessor = getDataServicesConfigProcessor("defaultSecured");
            Set<MessageInterceptor> interceptors = getMessageInterceptors(dataServicesConfigProcessor);

            // Should contain org.springframework.flex.security3.PerClientAuthenticationInterceptor and,
            // org.springframework.flex.security3.LoginMessageInterceptor
            assertEquals(2, interceptors.size());

            // Should contain org.springframework.flex.security3.SecurityExceptionTranslator
            Set<ExceptionTranslator> translators = getExceptionTranslators(dataServicesConfigProcessor);
            assertEquals(1, translators.size());
            assertEquals(SpringSecurityConfigResolver.resolve().getSecurityExceptionTranslatorClassName(),
                translators.iterator().next().getClass().getName());

            return;
        } catch(NoSuchBeanDefinitionException e) {
            fail("Expected " + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX + "to be registered");
        } catch (Exception e) {
            fail("Unexpected exception:" + e);
        }
    }
    
    public void testMessageBroker_DisabledHandlerMapping() {
        this.broker = applicationContext.getBean("disabledHandlerMapping", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        assertFalse("Default handler mapping not disabled", applicationContext.containsBean("disabledHandlerMappingDefaultHandlerMapping"));
    }

    public void testMessageBroker_EndpointSecured() {
        this.broker = applicationContext.getBean("endpointSecured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        for (Endpoint endpoint : this.broker.getEndpoints().values()) {
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[1];
            assertTrue("MessageInterception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
            Iterator<MessageInterceptor> m = ((MessageInterceptionAdvice) a.getAdvice()).getMessageInterceptors().iterator();
            while (m.hasNext()) {
                MessageInterceptor interceptor = m.next();
                if (interceptor instanceof EndpointInterceptor) {
                    Collection<ConfigAttribute> definitions = ((EndpointInterceptor) interceptor).getObjectDefinitionSource().getAllConfigAttributes();
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

    public void testMessageBroker_LoginCommandConfigured() {
        this.broker = applicationContext.getBean("loginCommandConfigured", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", this.broker);
        SpringSecurityLoginCommand loginCommand = (SpringSecurityLoginCommand) this.broker.getLoginManager().getLoginCommand();
        assertNotNull("LoginCommand not found", loginCommand);
        assertTrue("perClientAuthentication not configured", loginCommand.isPerClientAuthentication());

        for (Endpoint endpoint : this.broker.getEndpoints().values()) {
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
        }
    }

    public void testMessageBrokerDefaultConfig() {
        this.broker = applicationContext.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
        assertNotNull("MessageBroker bean not found for default ID", this.broker);
        assertTrue("MessageBroker should be started", this.broker.isStarted());
        assertNotNull("MessageBroker should have a RemotingService", this.broker.getServiceByType(RemotingService.class.getName()));
        assertNotNull("MessageBrokerHandlerAdapter not found", applicationContext.getBean(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER,
            MessageBrokerHandlerAdapter.class));
        SimpleUrlHandlerMapping defaultMapping = (SimpleUrlHandlerMapping) applicationContext.getBean(
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
    
    public static final class TestResourceHandlingInterceptor implements ResourceHandlingMessageInterceptor {

        public void afterCompletion(MessageProcessingContext context, Message inputMessage, Message outputMessage, Exception ex) {
            
        }

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
    
    public static final class ParentContextLoader extends MessageBrokerContextLoader {
        @Override
        protected ConfigurableApplicationContext createParentContext() {
            GenericWebApplicationContext context = new GenericWebApplicationContext();
            context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
            new XmlBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/config/security-context.xml", "classpath:org/springframework/flex/core/io/hibernate-jpa-context.xml" });
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
            context.refresh();
            context.registerShutdownHook();
            return context;
        }
    }

    /**
     * Returns the Data Services Config processor associated with given message broker bean
     */
    private Object getDataServicesConfigProcessor(String messageBrokerId) throws ClassNotFoundException {
        Object dataServicesConfigProcessor = applicationContext.getBean(
            messageBrokerId + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX, 
            Class.forName(MessageBrokerBeanDefinitionParser.DATASERVICES_PROCESSOR_CLASS_NAME));
        return dataServicesConfigProcessor;
    }

    /**
     * Returns the exception translators associated with the passed Services Config processor
     */
    @SuppressWarnings("unchecked")
    private Set<ExceptionTranslator> getExceptionTranslators(Object dataServicesConfigProcessor) 
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName(MessageBrokerBeanDefinitionParser.DATASERVICES_PROCESSOR_CLASS_NAME);
        Method getExceptionTranslatorsMethod = clazz.getMethod("getExceptionTranslators", (Class<?>[])null);
        return (Set<ExceptionTranslator>)getExceptionTranslatorsMethod.invoke(dataServicesConfigProcessor, (Object[])null);
    }

    /**
     * Returns the message interceptors associated with the passed Services Config processor
     */
    @SuppressWarnings("unchecked")
    private Set<MessageInterceptor> getMessageInterceptors(Object dataServicesConfigProcessor)
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName(MessageBrokerBeanDefinitionParser.DATASERVICES_PROCESSOR_CLASS_NAME);
        Method getMessageInterceptorsMethod = clazz.getMethod("getMessageInterceptors", (Class<?>[])null);
        return (Set<MessageInterceptor>)getMessageInterceptorsMethod.invoke(dataServicesConfigProcessor, (Object[])null);
    }    
}
