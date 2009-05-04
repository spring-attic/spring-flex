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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Configures a MessageDestinationFactory with a JmsAdapter from a top-level
 * <code>jms-message-destination</code> element.     
 * 
 * @author Mark Fisher
 * @author Jeremy Grelle
 */
public class JmsMessageDestinationBeanDefinitionParser extends AbstractMessageDestinationBeanDefinitionParser {
	
	// --------------------------- Full qualified class names ----------------//
	private static final String JMS_ADAPTER_CLASS_NAME = "org.springframework.flex.messaging.jms.JmsAdapter";

	// --------------------------- XML Config Attributes ---------------------//
	private static final String CONNECTION_FACTORY_ATTR = "connection-factory";
	private static final String JMS_DESTINATION_ATTR = "jms-destination";
	private static final String TOPIC_NAME_ATTR = "topic-name";
	private static final String QUEUE_NAME_ATTR = "queue-name";
	private static final String DESTINATION_RESOLVER_ATTR = "destination-resolver";
	private static final String MESSAGE_CONVERTER_ATTR = "message-converter";
	private static final String TRANSACTION_MANAGER_ATTR = "transaction-manager";
	
	
	// --------------------------- Bean Configuration Properties -------------//
	private static final String SERVICE_ADAPTER_PROPERTY = "serviceAdapter";
	private static final String CONNECTION_FACTORY_PROPERTY = "connectionFactory";
		
	// --------------------------- Default Values ----------------------------//
	private static final String DEFAULT_CONNECTION_FACTORY_REF = "connectionFactory";

	@Override
	protected void parseAdapter(Element element, ParserContext parserContext, BeanDefinitionBuilder destinationBuilder) {
		BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(JMS_ADAPTER_CLASS_NAME);
		
		int count = ParsingUtils.countProvidedAttributeValues(element, JMS_DESTINATION_ATTR, QUEUE_NAME_ATTR, TOPIC_NAME_ATTR);
		if (count != 1) {
			parserContext.getReaderContext().error(
					"exactly one of 'jms-destination', 'queue-name', or 'topic-name' is required", element);
			return;
		}
		
		String connectionFactoryId = element.getAttribute(CONNECTION_FACTORY_ATTR);
		connectionFactoryId = StringUtils.hasText(connectionFactoryId) ? connectionFactoryId : DEFAULT_CONNECTION_FACTORY_REF;
		adapterBuilder.addPropertyReference(CONNECTION_FACTORY_PROPERTY, connectionFactoryId);
		
		ParsingUtils.mapOptionalBeanRefAttributes(element, adapterBuilder, JMS_DESTINATION_ATTR, DESTINATION_RESOLVER_ATTR, 
				MESSAGE_CONVERTER_ATTR, TRANSACTION_MANAGER_ATTR);
		ParsingUtils.mapOptionalAttributes(element, adapterBuilder, QUEUE_NAME_ATTR, TOPIC_NAME_ATTR);
		
		String serviceAdapterId = ParsingUtils.registerInfrastructureComponent(element, parserContext, adapterBuilder);
		destinationBuilder.addPropertyValue(SERVICE_ADAPTER_PROPERTY, serviceAdapterId);
	}

}
