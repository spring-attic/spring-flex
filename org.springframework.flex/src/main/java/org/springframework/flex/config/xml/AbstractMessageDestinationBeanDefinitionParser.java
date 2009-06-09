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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.messaging.MessageDestinationFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * Base {@link BeanDefinitionParser} implementation for all parsers that configure a {@link MessageDestinationFactory}
 * bean.
 * 
 * <p>
 * Provides support for configuring the common network and server settings of all {@link MessagingAdapter}
 * implementations.
 * 
 * @author Jeremy Grelle
 */
public abstract class AbstractMessageDestinationBeanDefinitionParser extends AbstractBeanDefinitionParser {

    // --------------------------- Full qualified class names ----------------//
    private static final String DESTINATION_FACTORY_CLASS_NAME = "org.springframework.flex.messaging.MessageDestinationFactory";

    // --------------------------- XML Config Attributes ---------------------//
    private static final String MESSAGE_BROKER_ATTR = "message-broker";

    private static final String CHANNELS_ATTR = "channels";

    private static final String SUBSCRIPTION_TIMEOUT_ATTR = "subscription-timeout-minutes";

    private static final String THROTTLE_INBOUND_FREQ_ATTR = "throttle-inbound-max-frequency";

    private static final String THROTTLE_INBOUND_POLICY_ATTR = "throttle-inbound-policy";

    private static final String THROTTLE_OUTBOUND_FREQ_ATTR = "throttle-outbound-max-frequency";

    private static final String THROTTLE_OUTBOUND_POLICY_ATTR = "throttle-outbound-policy";

    private static final String ALLOW_SUBTOPICS_ATTR = "allow-subtopics";

    private static final String CLUSTER_ROUTING_ATTR = "cluster-message-routing";

    private static final String MESSAGE_TTL_ATTR = "message-time-to-live";

    private static final String SEND_CONSTRAINT_ATTR = "send-security-constraint";

    private static final String SUBSCRIBE_CONSTRAINT_ATTR = "subscribe-security-constraint";

    private static final String SUBTOPIC_SEPARATOR_ATTR = "subtopic-separator";

    // --------------------------- Bean Configuration Properties -------------//
    private static final String MESSAGE_BROKER_PROPERTY = "messageBroker";

    /**
     * Hook for subclasses to add custom parsing logic for technology-specific adapters.
     * 
     * @param element the xml element being parsed
     * @param parserContext the current parser context
     * @param destinationBuilder the builder for the {@link MessageDestinationFactory} bean being defined
     */
    protected abstract void parseAdapter(Element element, ParserContext parserContext, BeanDefinitionBuilder destinationBuilder);

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder destinationBuilder = BeanDefinitionBuilder.genericBeanDefinition(DESTINATION_FACTORY_CLASS_NAME);

        String brokerId = element.getAttribute(MESSAGE_BROKER_ATTR);
        brokerId = StringUtils.hasText(brokerId) ? brokerId : BeanIds.MESSAGE_BROKER;
        destinationBuilder.addPropertyReference(MESSAGE_BROKER_PROPERTY, brokerId);

        ParsingUtils.mapOptionalAttributes(element, parserContext, destinationBuilder, CHANNELS_ATTR, SUBSCRIPTION_TIMEOUT_ATTR,
            THROTTLE_INBOUND_FREQ_ATTR, THROTTLE_INBOUND_POLICY_ATTR, THROTTLE_OUTBOUND_FREQ_ATTR, THROTTLE_OUTBOUND_POLICY_ATTR, ALLOW_SUBTOPICS_ATTR,
            CLUSTER_ROUTING_ATTR, MESSAGE_TTL_ATTR, SEND_CONSTRAINT_ATTR, SUBSCRIBE_CONSTRAINT_ATTR, SUBTOPIC_SEPARATOR_ATTR);

        parseAdapter(element, parserContext, destinationBuilder);

        return destinationBuilder.getBeanDefinition();
    }

}
