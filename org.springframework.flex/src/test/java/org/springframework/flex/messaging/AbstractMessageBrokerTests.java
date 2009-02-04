package org.springframework.flex.messaging;

import junit.framework.TestCase;

import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;

public abstract class AbstractMessageBrokerTests extends TestCase {

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
		context.setServletContext(new MockServletContext());
		mbfb = new MessageBrokerFactoryBean();
		mbfb.setServletContext(context.getServletContext());
		mbfb.setResourceLoader(context);
		mbfb.setBeanName("testMessageBroker");
		mbfb.setBeanClassLoader(context.getClassLoader());
		mbfb.setServicesConfigPath("classpath:org/springframework/flex/messaging/services-config.xml");
		mbfb.afterPropertiesSet();
		
		return (MessageBroker) mbfb.getObject();
	}

}
