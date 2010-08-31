package org.springframework.flex.config.xml;

import java.util.Iterator;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.config.TestWebInfResourceLoader;
import org.springframework.flex.core.ExceptionTranslationAdvice;
import org.springframework.flex.core.MessageInterceptionAdvice;
import org.springframework.flex.security3.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.flex.security3.SpringSecurityLoginCommand;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;

import flex.messaging.MessageBroker;
import flex.messaging.security.LoginCommand;

@ContextConfiguration("classpath:/org/springframework/flex/config/secured-message-broker.xml")
public class MessageBrokerBeanDefinitionParserNoParentContextTests extends AbstractFlexConfigurationTests {

    @SuppressWarnings("unchecked")
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
        applicationContext.getBean(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, FlexSessionInvalidatingAuthenticationListener.class);
        RequestContextFilter filter = (RequestContextFilter) applicationContext.getBean(BeanIds.REQUEST_CONTEXT_FILTER, RequestContextFilter.class);
        FilterChainProxy filterChain = (FilterChainProxy) applicationContext.getBean("org.springframework.security.filterChainProxy", FilterChainProxy.class);
        assertTrue(((List)filterChain.getFilterChainMap().get("/**")).contains(filter));
    }

}
