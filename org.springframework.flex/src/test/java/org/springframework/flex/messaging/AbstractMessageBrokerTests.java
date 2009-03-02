package org.springframework.flex.messaging;

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.flex.messaging.config.MessageBrokerConfigProcessor;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;

public abstract class AbstractMessageBrokerTests extends TestCase {

	private StaticWebApplicationContext context = new StaticWebApplicationContext();
	private MessageBrokerFactoryBean mbfb;
	private Set<MessageBrokerConfigProcessor> startupProcessors = new LinkedHashSet<MessageBrokerConfigProcessor>();
	private String currentConfigPath;

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
		mbfb.setBeanName(super.getName()+"MessageBroker");
		mbfb.setBeanClassLoader(context.getClassLoader());
		currentConfigPath = getServicesConfigPath();
		mbfb.setServicesConfigPath(currentConfigPath);
		mbfb.setConfigProcessors(startupProcessors);
		mbfb.afterPropertiesSet();
		
		return (MessageBroker) mbfb.getObject();
	}
	
	protected String getServicesConfigPath() {
		return "classpath:org/springframework/flex/messaging/services-config.xml";
	}
	
	protected String getCurrentConfigPath() {
		return currentConfigPath;
	}
	
	protected final void setDirty() {
		if (FlexContext.getMessageBroker() != null) {
			FlexContext.getMessageBroker().stop();
			FlexContext.setThreadLocalObjects(null, null, null, null, null, null);
		}
		startupProcessors.clear();
	}
	
	protected final void addStartupProcessor(MessageBrokerConfigProcessor processor) {
		startupProcessors.add(processor);
	}

}
