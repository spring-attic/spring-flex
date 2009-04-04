package org.springframework.flex.config.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers the bean definition parsers for the "flex" namespace (http://www.springframework.org/schema/flex).
 *
 * @author Jeremy Grelle
 * @author Mark Fisher
 */
public class FlexNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("message-broker", new MessageBrokerBeanDefinitionParser());
		registerBeanDefinitionParser("remoting-destination", new RemotingDestinationBeanDefinitionParser());
		registerBeanDefinitionParser("jms-message-destination", new JmsMessageDestinationBeanDefinitionParser());
		registerBeanDefinitionDecorator("remoting-destination", new RemotingDestinationBeanDefinitionDecorator());
	}

}
