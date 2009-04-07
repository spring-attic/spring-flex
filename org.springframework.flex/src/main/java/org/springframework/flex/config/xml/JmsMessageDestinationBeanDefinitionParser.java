/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.config.BeanIds;
import org.springframework.util.StringUtils;

/**
 * Configures a MessageDestinationFactory with a JmsAdapter from a top-level
 * <code>jms-message-destination</code> element.     
 * 
 * @author Mark Fisher
 */
public class JmsMessageDestinationBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String MESSAGING_PACKAGE = "org.springframework.flex.messaging";

	private static final String DEFAULT_CONNECTION_FACTORY_REF = "connectionFactory";


	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder destinationBuilder = BeanDefinitionBuilder.genericBeanDefinition(
				MESSAGING_PACKAGE + ".MessageDestinationFactory");
		BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(
				MESSAGING_PACKAGE + ".jms.JmsAdapter");
		String destinationRef = element.getAttribute("jms-destination-ref");
		String queueName = element.getAttribute("queue-name");
		String topicName = element.getAttribute("topic-name");
		int count = this.countProvidedAttributeValues(destinationRef, queueName, topicName);
		if (count != 1) {
			parserContext.getReaderContext().error(
					"exactly one of 'jms-destination-ref', 'queue-name', or 'topic-name' is required", element);
			return null;
		}
		if (StringUtils.hasText(destinationRef)) {
			adapterBuilder.addPropertyReference("jmsDestination", destinationRef);
		}
		if (StringUtils.hasText(queueName)) {
			adapterBuilder.addPropertyValue("queueName", queueName);
		}
		if (StringUtils.hasText(topicName)) {
			adapterBuilder.addPropertyValue("topicName", topicName);
		}
		String connectionFactoryRef = element.getAttribute("connection-factory");
		connectionFactoryRef = StringUtils.hasText(connectionFactoryRef) ?
				connectionFactoryRef : DEFAULT_CONNECTION_FACTORY_REF;
		adapterBuilder.addPropertyReference("connectionFactory", connectionFactoryRef);
		String adapterBeanName = BeanDefinitionReaderUtils.registerWithGeneratedName(
				adapterBuilder.getBeanDefinition(), parserContext.getRegistry());
		destinationBuilder.addPropertyValue("adapterBeanName", adapterBeanName);
		String brokerId = element.getAttribute("message-broker");
		brokerId = StringUtils.hasText(brokerId) ? brokerId : BeanIds.MESSAGE_BROKER; 
		destinationBuilder.addPropertyReference("messageBroker", brokerId);
		String channels = element.getAttribute("channels");
		if (StringUtils.hasText(channels)) {
			destinationBuilder.addPropertyValue("channels", channels);
		}
		return destinationBuilder.getBeanDefinition();
	}

	private int countProvidedAttributeValues(String... values) {
		int count = 0;
		for (String s : values) {
			if (StringUtils.hasText(s)) {
				count++;
			}
		}
		return count;
	}

}
