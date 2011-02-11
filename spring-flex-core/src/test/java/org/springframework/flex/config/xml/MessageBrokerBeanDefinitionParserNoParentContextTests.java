package org.springframework.flex.config.xml;

import java.util.Iterator;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.core.ExceptionTranslationAdvice;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.flex.security3.SecurityConfigurationPostProcessor;
import org.springframework.flex.security3.SpringSecurityLoginCommand;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import flex.messaging.MessageBroker;
import flex.messaging.security.LoginCommand;

@ContextConfiguration("classpath:/org/springframework/flex/config/secured-message-broker.xml")
public class MessageBrokerBeanDefinitionParserNoParentContextTests extends AbstractFlexConfigurationTests {

    @SuppressWarnings("rawtypes")
	public void testMessageBroker_DefaultSecured() {
        MessageBroker broker = (MessageBroker) applicationContext.getBean("defaultSecured2", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", broker);
        LoginCommand loginCommand = broker.getLoginManager().getLoginCommand();
        assertNotNull("LoginCommand not found", loginCommand);
        assertTrue("LoginCommand of wrong type", loginCommand instanceof SpringSecurityLoginCommand);
        assertSame("LoginCommand not a managed spring bean", loginCommand, applicationContext.getBean("defaultSecured2LoginCommand"));
        Iterator i = broker.getEndpoints().values().iterator();
        while (i.hasNext()) {
            Object endpoint = i.next();
            assertTrue("Endpoint should be proxied", AopUtils.isAopProxy(endpoint));
            Advised advisedEndpoint = (Advised) endpoint;
            Advisor a = advisedEndpoint.getAdvisors()[0];
            assertTrue("Exception translation advice was not applied", a.getAdvice() instanceof ExceptionTranslationAdvice);
            a = advisedEndpoint.getAdvisors()[1];
            assertTrue("Message interception advice was not applied", a.getAdvice() instanceof MessageInterceptionAdvice);
        }
        SecurityConfigurationPostProcessor processor = applicationContext.getBean(SecurityConfigurationPostProcessor.class);
        assertNotNull("Security config processor not found", processor);
        assertSame(ReflectionTestUtils.getField(processor, "sessionAuthenticationStrategy"), ReflectionTestUtils.getField(loginCommand, "sessionStrategy"));
    }

}
