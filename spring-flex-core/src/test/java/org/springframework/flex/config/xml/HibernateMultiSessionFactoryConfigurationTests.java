package org.springframework.flex.config.xml;

import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.flex.config.AbstractFlexConfigurationTests;
import org.springframework.flex.config.MessageBrokerContextLoader;
import org.springframework.flex.config.TestWebInfResourceLoader;
import org.springframework.flex.core.io.HibernateConfigProcessor;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.MessageBroker;

@ContextConfiguration(locations="classpath:org/springframework/flex/config/hibernate-message-broker.xml", inheritLocations=false, loader=HibernateMultiSessionFactoryConfigurationTests.ParentContextLoader.class)
public class HibernateMultiSessionFactoryConfigurationTests extends AbstractFlexConfigurationTests {

    public void testMessageBroker_MultiSessionFactory() {
        GenericWebApplicationContext parent = (GenericWebApplicationContext) new ParentContextLoader().createParentContext();
        try {
            new ClassPathXmlApplicationContext(new String[] {"org/springframework/flex/config/invalid-message-broker.xml"}, parent);
            fail("Invalid message-broker config was not caught");
        } catch (BeanDefinitionParsingException ex) {
            // Expected
            parent.close();
        }
    }
    
    public void testMessageBroker_CustomConfigProcessor() {
        MessageBroker broker = applicationContext.getBean("multiSessionFactoryMessageBroker", MessageBroker.class);
        assertNotNull(broker);
        assertTrue(applicationContext.getBeansOfType(HibernateConfigProcessor.class).entrySet().size() == 1);
    }
    
    public static final class ParentContextLoader extends MessageBrokerContextLoader {
        @Override
        protected ConfigurableApplicationContext createParentContext() {
            GenericWebApplicationContext context = new GenericWebApplicationContext();
            context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
            new XmlBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/core/io/hibernate-multi-session-factory-context.xml" });
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
            context.refresh();
            context.registerShutdownHook();
            return context;
        }
    }
}
