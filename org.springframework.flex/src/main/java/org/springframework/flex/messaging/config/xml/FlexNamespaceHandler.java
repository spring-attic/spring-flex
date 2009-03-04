package org.springframework.flex.messaging.config.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers the bean definition parsers for the "flex" namespace (http://www.springframework.org/schema/flex).
 *
 * @author Jeremy Grelle
 */
public class FlexNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("message-broker", new MessageBrokerBeanDefinitionParser());
		registerBeanDefinitionParser("remote-service", new RemoteServiceBeanDefinitionParser());
		
		registerBeanDefinitionDecorator("remote-service", new RemoteServiceBeanDefinitionDecorator());
	}

}
