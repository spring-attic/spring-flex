package org.springframework.flex.hibernate3.config.xml;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.flex.hibernate3.config.AbstractFlexConfigurationTests;
import org.springframework.flex.hibernate3.config.MessageBrokerContextLoader;
import org.springframework.flex.hibernate3.config.TestWebInfResourceLoader;
import org.springframework.flex.orm.hibernate3.HibernateConfigProcessor;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.MessageBroker;

@ContextConfiguration(locations="classpath:org/springframework/flex/hibernate3/config/hibernate-message-broker.xml", inheritLocations=false, loader=HibernateMultiSessionFactoryConfigurationTests.ParentContextLoader.class)
public class HibernateMultiSessionFactoryConfigurationTests extends AbstractFlexConfigurationTests {
    
    public void testMessageBroker_AutoDetectMultipleSessionFactories() {
        MessageBroker broker = applicationContext.getBean("multiSessionFactoryMessageBroker", MessageBroker.class);
        assertNotNull(broker);
        assertNotNull(applicationContext.getBean("sessionFactory"));
        assertNotNull(applicationContext.getBean("sessionFactory2"));
        assertEquals(1, applicationContext.getBeansOfType(HibernateConfigProcessor.class).entrySet().size());
    }
    
    public static final class ParentContextLoader extends MessageBrokerContextLoader {
        @Override
        protected ConfigurableApplicationContext createParentContext() {
            GenericWebApplicationContext context = new GenericWebApplicationContext();
            context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
            new XmlBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/hibernate3/core/io/hibernate-multi-session-factory-context.xml" });
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
            context.refresh();
            context.registerShutdownHook();
            return context;
        }
    }
}
