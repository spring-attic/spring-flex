package org.springframework.flex.config;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.support.GenericWebApplicationContext;


public class MessageBrokerContextLoader extends AbstractContextLoader {

	/**
	 * Loads a new {@link org.springframework.context.ApplicationContext context} based on the supplied
	 * {@link org.springframework.test.context.MergedContextConfiguration merged context configuration},
	 * configures the context, and finally returns the context in a fully
	 * <em>refreshed</em> state.
	 * <p>Concrete implementations should register annotation configuration
	 * processors with bean factories of
	 * {@link org.springframework.context.ApplicationContext application contexts} loaded by this
	 * {@code SmartContextLoader}. Beans will therefore automatically be
	 * candidates for annotation-based dependency injection using
	 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired},
	 * {@link javax.annotation.Resource @Resource}, and
	 * {@link javax.inject.Inject @Inject}. In addition, concrete implementations
	 * should set the active bean definition profiles in the context's
	 * {@link org.springframework.core.env.Environment Environment}.
	 * <p>Any <code>ApplicationContext</code> loaded by a
	 * {@code SmartContextLoader} <strong>must</strong> register a JVM
	 * shutdown hook for itself. Unless the context gets closed early, all context
	 * instances will be automatically closed on JVM shutdown. This allows for
	 * freeing of external resources held by beans within the context (e.g.,
	 * temporary files).
	 *
	 * @param mergedConfig the merged context configuration to use to load the
	 *                     application context
	 * @return a new application context
	 * @throws Exception if context loading failed
	 * @see #processContextConfiguration(org.springframework.test.context.ContextConfigurationAttributes)
	 * @see org.springframework.context.annotation.AnnotationConfigUtils#registerAnnotationConfigProcessors()
	 * @see org.springframework.test.context.MergedContextConfiguration#getActiveProfiles()
	 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
	 */
	public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
		return loadContext(mergedConfig.getLocations());
	}

	/**
	 * Loads a new {@link org.springframework.context.ApplicationContext context} based on the supplied
	 * <code>locations</code>, configures the context, and finally returns
	 * the context in fully <em>refreshed</em> state.
	 * <p>Configuration locations are generally considered to be classpath
	 * resources by default.
	 * <p>Concrete implementations should register annotation configuration
	 * processors with bean factories of {@link org.springframework.context.ApplicationContext application
	 * contexts} loaded by this ContextLoader. Beans will therefore automatically
	 * be candidates for annotation-based dependency injection using
	 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired},
	 * {@link javax.annotation.Resource @Resource}, and
	 * {@link javax.inject.Inject @Inject}.
	 * <p>Any ApplicationContext loaded by a ContextLoader <strong>must</strong>
	 * register a JVM shutdown hook for itself. Unless the context gets closed
	 * early, all context instances will be automatically closed on JVM
	 * shutdown. This allows for freeing external resources held by beans within
	 * the context, e.g. temporary files.
	 *
	 * @param locations the resource locations to use to load the application context
	 * @return a new application context
	 * @throws Exception if context loading failed
	 */
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

    @Override
    protected String getResourceSuffix() {
        return "-context.xml";
    }
    
    protected ConfigurableApplicationContext createParentContext() {
        return null;
    }

}
