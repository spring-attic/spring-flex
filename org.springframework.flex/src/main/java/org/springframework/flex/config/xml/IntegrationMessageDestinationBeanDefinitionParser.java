/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.messaging.MessageDestinationFactory;
import org.springframework.flex.messaging.integration.IntegrationAdapter;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} implementation for parsing the <code>integration-message-destination</code> element.
 * 
 * <p>
 * Configures the {@link MessageDestinationFactory} bean definition with the appropriate {@link IntegrationAdapter}
 * class for connecting to Spring Integration message channels.
 * 
 * @author Jeremy Grelle
 */
public class IntegrationMessageDestinationBeanDefinitionParser extends AbstractMessageDestinationBeanDefinitionParser {

    // --------------------------- Full qualified class names ----------------//
    private static final String INTEGRATION_ADAPTER_CLASS_NAME = "org.springframework.flex.messaging.integration.IntegrationAdapter";

    // --------------------------- XML Config Attributes ---------------------//
    private static final String MESSAGE_CHANNEL_ATTR = "message-channel";

    private static final String EXTRACT_PAYLOAD_ATTR = "extract-payload";

    // --------------------------- Bean Configuration Properties -------------//
    private static final String SERVICE_ADAPTER_PROPERTY = "serviceAdapter";

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void parseAdapter(Element element, ParserContext parserContext, BeanDefinitionBuilder destinationBuilder) {
        BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(INTEGRATION_ADAPTER_CLASS_NAME);

        ParsingUtils.mapRequiredBeanRefAttributes(element, adapterBuilder, MESSAGE_CHANNEL_ATTR);
        ParsingUtils.mapOptionalAttributes(element, adapterBuilder, EXTRACT_PAYLOAD_ATTR);

        String serviceAdapterId = ParsingUtils.registerInfrastructureComponent(element, parserContext, adapterBuilder);
        destinationBuilder.addPropertyValue(SERVICE_ADAPTER_PROPERTY, serviceAdapterId);
    }

}
