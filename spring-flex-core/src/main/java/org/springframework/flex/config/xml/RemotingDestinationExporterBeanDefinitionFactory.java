/*
 * Copyright 2002-2011 the original author or authors.
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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.remoting.RemotingDestinationExporter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Shared base class for building {@link RemotingDestinationExporter} bean definitions.
 * 
 * @author Jeremy Grelle
 */
abstract class RemotingDestinationExporterBeanDefinitionFactory {

    // --------------------------- Full qualified class names ----------------//
    protected static final String REMOTING_DESTINATION_EXPORTER_CLASS_NAME = "org.springframework.flex.remoting.RemotingDestinationExporter";

    // --------------------------- XML Config Attributes ---------------------//
    private static final String MESSAGE_BROKER_ATTR = "message-broker";

    private static final String DESTINATION_ID_ATTR = "destination-id";

    private static final String CHANNELS_ATTR = "channels";

    private static final String INCLUDE_METHODS_ATTR = "include-methods";

    private static final String EXCLUDE_METHODS_ATTR = "exclude-methods";

    private static final String SERVICE_ADAPTER_ATTR = "service-adapter";

    protected static final String REF_ATTR = "ref";

    // --------------------------- Bean Configuration Properties -------------//
    private static final String MESSAGE_BROKER_PROPERTY = "messageBroker";

    private static final String SERVICE_PROPERTY = "service";

    private static final String DESTINATION_ID_PROPERTY = "destinationId";

    /**
     * The main parsing algorithm for the <code>remoting-destination</code> tag.
     * 
     * @param element the element being parsed
     * @param parserContext the current parser context
     * @param exportedBeanReference reference to the bean being exported as a remoting destination
     * @return a bean definition holder for the {@link RemotingDestinationExporter} bean
     */
    protected BeanDefinitionHolder parseInternal(Element element, ParserContext parserContext, String exportedBeanReference) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(REMOTING_DESTINATION_EXPORTER_CLASS_NAME);
        builder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        validateRemotingDestination(element, parserContext);

        String destinationId = element.getAttribute(DESTINATION_ID_ATTR);
        String brokerId = element.getAttribute(MESSAGE_BROKER_ATTR);

        builder.addPropertyValue(SERVICE_PROPERTY, exportedBeanReference);
        builder.addDependsOn(exportedBeanReference);

        destinationId = StringUtils.hasText(destinationId) ? destinationId : exportedBeanReference;
        builder.addPropertyValue(DESTINATION_ID_PROPERTY, destinationId);

        brokerId = StringUtils.hasText(brokerId) ? brokerId : BeanIds.MESSAGE_BROKER;
        builder.addPropertyReference(MESSAGE_BROKER_PROPERTY, brokerId);

        ParsingUtils.mapOptionalAttributes(element, parserContext, builder, CHANNELS_ATTR, INCLUDE_METHODS_ATTR, EXCLUDE_METHODS_ATTR, SERVICE_ADAPTER_ATTR);

        String beanName = ParsingUtils.registerInfrastructureComponent(element, parserContext, builder);

        return new BeanDefinitionHolder(builder.getBeanDefinition(), beanName);
    }

    /**
     * Hook for subclasses to perform contextual validation on the element being parsed.
     * 
     * @param element the element being parsed
     * @param parserContext the current parser context
     */
    protected abstract void validateRemotingDestination(Element element, ParserContext parserContext);
}
