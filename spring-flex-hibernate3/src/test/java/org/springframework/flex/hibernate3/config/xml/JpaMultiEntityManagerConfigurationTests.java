package org.springframework.flex.hibernate3.config.xml;

import org.junit.Assert;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.flex.hibernate3.config.AbstractFlexConfigurationTests;
import org.springframework.flex.hibernate3.config.MessageBrokerContextLoader;
import org.springframework.flex.hibernate3.config.TestWebInfResourceLoader;
import org.springframework.flex.hibernate3.config.JpaHibernateConfigProcessor;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.MessageBroker;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

@ContextConfiguration(locations="classpath:org/springframework/flex/hibernate3/config/jpa-message-broker.xml", inheritLocations=false, loader=JpaMultiEntityManagerConfigurationTests.ParentContextLoader.class)
public class JpaMultiEntityManagerConfigurationTests extends
        AbstractFlexConfigurationTests {

    @Test
    public void autoDetectMultipleEntityManagers() {
        MessageBroker broker = applicationContext.getBean("multiEntityManagerMessageBroker", MessageBroker.class);
        assertNotNull(broker);
        assertNotNull(applicationContext.getBean("entityManagerFactory"));
        assertNotNull(applicationContext.getBean("entityManagerFactory2"));
        Assert.assertEquals(1, applicationContext.getBeansOfType(JpaHibernateConfigProcessor.class).entrySet().size());
    }

    public static final class ParentContextLoader extends MessageBrokerContextLoader {
        @Override
        protected ConfigurableApplicationContext createParentContext() {
            GenericWebApplicationContext context = new GenericWebApplicationContext();
            context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
            new XmlBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/hibernate3/jpa-multi-entity-manager-context.xml" });
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
            context.refresh();
            context.registerShutdownHook();
            return context;
        }
    }
}
