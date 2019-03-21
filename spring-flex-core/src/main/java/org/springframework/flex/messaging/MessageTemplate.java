/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.flex.config.BeanIds;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;
import flex.messaging.util.UUIDUtils;

/**
 * Simple helper for sending Flex {@link AsyncMessage}s from a Java client. The message will be routed through the
 * {@link MessageBroker} to the specified {@link MessageDestination}. This allows for flexible routing of the message
 * using whatever {@link MessagingAdapter} is configured for the target destination, be it the basic BlazeDS
 * {@link ActionScriptAdapter}, one of the provided Spring adapters such as {@link org.springframework.flex.messaging.jms.JmsAdapter} 
 * or {@link org.springframework.flex.messaging.integration.IntegrationAdapter}, or some other custom adapter implementation.
 * 
 * <p>This class should typically be configured as a Spring bean in the same ApplicationContext as the &lt;flex:message-broker&gt; tag 
 * to allow injection of a Spring-managed {@link MessageBroker} instance. When a single <code>MessageBroker</code> is detected in the 
 * ApplicationContext, it will be automatically injected into the MessageTemplate bean.  If more than one <code>MessageBroker</code> is 
 * present (not typical), a reference to the correct <code>MessageBroker</code> must be explicitly set for the {@link MessageTemplate#setMessageBroker(MessageBroker)} 
 * property.
 * 
 * <p>The <code>MessageTemplate</code> may also be instantiated directly, in which case it will try to look up the ThreadLocal 
 * <code>MessageBroker</code> instance for the current request, but configuration as a Spring bean is strongly preferred.
 * 
 * @author Jeremy Grelle
 */
public class MessageTemplate implements InitializingBean, BeanFactoryAware {

    private static final Log log = LogFactory.getLog(MessageTemplate.class);

    private String defaultDestination;

    private MessageBroker messageBroker;

    private BeanFactory beanFactory;

    private final String clientId = UUIDUtils.createUUID();

    private final AsyncMessageCreator defaultMessageCreator = new DefaultAsyncMessageCreator();

    /**
     * 
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (this.messageBroker == null) {
            // first try the default id
            if (this.beanFactory.containsBean(BeanIds.MESSAGE_BROKER)) {
                this.messageBroker = (MessageBroker) this.beanFactory.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
            } else if (this.beanFactory instanceof ListableBeanFactory) {
                ListableBeanFactory lbf = (ListableBeanFactory) this.beanFactory;
                String[] brokerNames = lbf.getBeanNamesForType(MessageBroker.class);
                if (brokerNames.length == 1) {
                    this.messageBroker = (MessageBroker) lbf.getBean(brokerNames[0]);
                } else if (brokerNames.length > 1) {
                    log.warn("A MessageBroker was not explicitly set and one could not be auto-detected in the MessageTemplate's bean factory as "
                        + "multiple MessageBrokers were found.  Will fall back to attempting to obtain the MessageBroker from the FlexContext for "
                        + "each operation.");
                } else {
                    log.warn("A MessageBroker was not explicitly set and one could not be found in the MessageTemplate's bean factory.  Will "
                        + "will fall back to attempting to obtain the MessageBroker from the FlexContext for each operation.");
                }
            }
        }
    }

    /**
     * Creates a default {@link AsyncMessage}
     * 
     * @return the created message
     */
    public AsyncMessage createMessage() {
        return this.defaultMessageCreator.createMessage();
    }

    /**
     * Creates a default {@link AsyncMessage} for a specified destination
     * 
     * @param destination the target destination for the message
     * @return the created message
     */
    public AsyncMessage createMessageForDestination(String destination) {
        AsyncMessage message = this.defaultMessageCreator.createMessage();
        message.setDestination(destination);
        return message;
    }

    /**
     * Returns the id of the default message destination for this template
     * 
     * @return the default destination id
     */
    public String getDefaultDestination() {
        return this.defaultDestination;
    }

    /**
     * Returns the {@link MessageBroker} for routing messages
     * 
     * @return the message broker
     */
    public MessageBroker getMessageBroker() {
        if (this.messageBroker != null) {
            return this.messageBroker;
        }
        Assert.notNull(FlexContext.getMessageBroker(), "A MessageBroker was not set on the MessageTemplate "
            + "and no thread-local MessageBroker could be found in the FlexContext.");
        return FlexContext.getMessageBroker();
    }

    /**
     * Sends a message created by the specified {@link AsyncMessageCreator}
     * 
     * @param creator the message creator
     */
    public void send(AsyncMessageCreator creator) {
        getMessageBroker().routeMessageToService(creator.createMessage(), null);
    }

    /**
     * Sends a message with the specified body to the default destination.
     * 
     * @param body the body of the message
     */
    public void send(Object body) {
        Assert.hasText(this.defaultDestination, "Cannot send message - no default destination has been set for this MessageTemplate.");
        send(this.defaultDestination, body);
    }

    /**
     * Sends a message with the specified body to the specified destination
     * 
     * @param destination the target destination id
     * @param body the body of the message
     */
    public void send(String destination, Object body) {
        AsyncMessage message = this.defaultMessageCreator.createMessage();
        message.setDestination(destination);
        message.setBody(body);
        getMessageBroker().routeMessageToService(message, null);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Sets the id of the default message destination for this template
     * 
     * @param defaultDestination the default destination id
     */
    public void setDefaultDestination(String defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

    /**
     * Sets the {@link MessageBroker} for routing messages
     * 
     * @param messageBroker the message broker
     */
    public void setMessageBroker(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    private final class DefaultAsyncMessageCreator implements AsyncMessageCreator {

        public AsyncMessage createMessage() {
            AsyncMessage message = new AsyncMessage();
            message.setClientId(MessageTemplate.this.clientId);
            message.setMessageId(UUIDUtils.createUUID());
            message.setTimestamp(System.currentTimeMillis());
            return message;
        }
    }
}
