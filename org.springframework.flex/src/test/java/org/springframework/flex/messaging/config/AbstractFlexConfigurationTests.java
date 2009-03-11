package org.springframework.flex.messaging.config;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.AbstractSingleSpringContextTests;
import org.springframework.web.context.support.GenericWebApplicationContext;

public abstract class AbstractFlexConfigurationTests extends
		AbstractSingleSpringContextTests {

	@Override
	protected ConfigurableApplicationContext createApplicationContext(
			String[] locations) {
		GenericWebApplicationContext context = new GenericWebApplicationContext();
		context.setServletContext(new MockServletContext(new WebInfResourceLoader(context)));
		prepareApplicationContext(context);
		customizeBeanFactory(context.getDefaultListableBeanFactory());
		createBeanDefinitionReader(context).loadBeanDefinitions(locations);
		context.refresh();
		return context;
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] {"classpath:org/springframework/flex/messaging/config/message-broker.xml",
				"classpath:org/springframework/flex/messaging/config/remote-service.xml",
				"classpath:org/springframework/flex/messaging/config/remote-service-decorator.xml"};
	}
}
