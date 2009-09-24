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
import org.springframework.flex.security.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.flex.security.SpringSecurityLoginCommand;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.util.FilterChainProxy;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;

import flex.messaging.MessageBroker;
import flex.messaging.security.LoginCommand;


public class MessageBrokerBeanDefinitionParserNoParentContextTests extends AbstractFlexConfigurationTests {

    @SuppressWarnings("unchecked")
    public void testMessageBroker_DefaultSecured() {
        MessageBroker broker = (MessageBroker) getApplicationContext().getBean("defaultSecured2", MessageBroker.class);
        assertNotNull("MessageBroker bean not found for custom id", broker);
        LoginCommand loginCommand = broker.getLoginManager().getLoginCommand();
        assertNotNull("LoginCommand not found", loginCommand);
        assertTrue("LoginCommand of wrong type", loginCommand instanceof SpringSecurityLoginCommand);
        assertSame("LoginCommand not a managed spring bean", loginCommand, getApplicationContext().getBean("defaultSecured2LoginCommand"));
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
        getApplicationContext().getBean(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, FlexSessionInvalidatingAuthenticationListener.class);
        RequestContextFilter filter = (RequestContextFilter) getApplicationContext().getBean(BeanIds.REQUEST_CONTEXT_FILTER, RequestContextFilter.class);
        FilterChainProxy filterChain = (FilterChainProxy) getApplicationContext().getBean(org.springframework.security.config.BeanIds.FILTER_CHAIN_PROXY, FilterChainProxy.class);
        assertTrue(((List)filterChain.getFilterChainMap().get("/**")).contains(filter));
    }
    
    @Override
    protected ConfigurableApplicationContext createApplicationContext(String[] locations) {
        
        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
        prepareApplicationContext(context);
        customizeBeanFactory(context.getDefaultListableBeanFactory());
        createBeanDefinitionReader(context).loadBeanDefinitions(locations);
        context.refresh();
        return context;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "classpath:/org/springframework/flex/config/secured-message-broker.xml" };
    }
}
