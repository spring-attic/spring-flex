package org.springframework.flex.config;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;


public class MessageBrokerContextLoader extends AbstractContextLoader {

    public ApplicationContext loadContext(String... locations) throws Exception {
        ConfigurableApplicationContext parentContext = createParentContext();

        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
        
        if (MessageBroker.getMessageBroker(BeanIds.MESSAGE_BROKER) != null) {
            FlexContext.clearThreadLocalObjects();
            MessageBroker.getMessageBroker(BeanIds.MESSAGE_BROKER).stop();
        }
        
        new XmlBeanDefinitionReader(context).loadBeanDefinitions(locations);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.setParent(parentContext);
        context.refresh();
        context.registerShutdownHook();
        return context;
    }

    public ApplicationContext loadContext(MergedContextConfiguration configuration) throws Exception {
        return loadContext(configuration.getLocations());
    }

    @Override
    protected String getResourceSuffix() {
        return "-context.xml";
    }
    
    protected ConfigurableApplicationContext createParentContext() {
        return null;
    }

}
