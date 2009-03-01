package org.springframework.flex.messaging.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class FlexNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("message-broker", new MessageBrokerBeanDefinitionParser());
		registerBeanDefinitionParser("remote-service", new RemoteServiceBeanDefinitionParser());
		
		registerBeanDefinitionDecorator("remote-service", new RemoteServiceBeanDefinitionDecorator());
	}

}
