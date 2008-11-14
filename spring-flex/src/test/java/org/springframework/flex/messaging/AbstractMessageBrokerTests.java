package org.springframework.flex.messaging;

import javax.servlet.ServletConfig;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.context.support.StaticWebApplicationContext;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import junit.framework.TestCase;

public abstract class AbstractMessageBrokerTests extends TestCase {

	private ServletConfig config = new MockServletConfig();
	private StaticWebApplicationContext context = new StaticWebApplicationContext();
	private MessageBrokerFactoryBean mbfb;

	protected final MessageBroker getMessageBroker() throws Exception {
		if (FlexContext.getMessageBroker() != null) {
			return FlexContext.getMessageBroker();
		} else {
			return createMessageBroker();
		}
		
	}

	protected final MessageBroker createMessageBroker() throws Exception {
		context.setServletConfig(config);
		mbfb = new MessageBrokerFactoryBean();
		mbfb.setServletConfig(config);
		mbfb.setResourceLoader(context);
		mbfb.setBeanName("testMessageBroker");
		mbfb.setBeanClassLoader(context.getClassLoader());
		mbfb.setServicesConfigPath("classpath:org/springframework/flex/messaging/services-config.xml");
		mbfb.afterPropertiesSet();
		
		return (MessageBroker) mbfb.getObject();
	}

}
