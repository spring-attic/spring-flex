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

package org.springframework.flex.messaging.integration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.PollableChannel;
import org.springframework.integration.channel.SubscribableChannel;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.message.MessageHandler;
import org.springframework.util.Assert;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * A {@link MessagingAdapter} implementation that enables sending and receiving messages via Spring Integration Message
 * Channels.
 * 
 * @author Mark Fisher
 */
public class IntegrationAdapter extends MessagingAdapter implements MessageHandler, InitializingBean, BeanNameAware {

    private final Log logger = LogFactory.getLog(getClass());

    private volatile MessageChannel messageChannel;

    private volatile boolean extractPayload = true;

    private final Set<Object> subscriberIds = new HashSet<Object>();

    private volatile AbstractEndpoint consumerEndpoint;

    /**
     * Specify whether the Flex Message body should be extracted
     * to be used as the payload of a Spring Integration Message.
     * If this is set to <code>false</code>, the entire Flex Message
     * will be sent as the payload. The default is <code>true</code>.
     */
    public void setExtractPayload(boolean extractPayload) {
        this.extractPayload = extractPayload;
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() {
        Assert.notNull(this.messageChannel, "MessageChannel must not be null");
        if (this.messageChannel instanceof PollableChannel) {
            this.consumerEndpoint = new PollingConsumer((PollableChannel) this.messageChannel, this);
        } else if (this.messageChannel instanceof SubscribableChannel) {
            this.consumerEndpoint = new EventDrivenConsumer((SubscribableChannel) this.messageChannel, this);
        }
    }

    /**
     * Invoked when a Message is received from the Spring Integration channel.
     */
    public void handleMessage(org.springframework.integration.core.Message<?> message) {
        if (logger.isDebugEnabled()) {
            logger.debug("received Integration Message: " + message);
        }
        AsyncMessage flexMessage = new AsyncMessage();
        flexMessage.setBody(message.getPayload());
        MessageHeaders headers = message.getHeaders();
        flexMessage.setMessageId(headers.getId().toString());
        flexMessage.setTimestamp(headers.getTimestamp().longValue());
        Long expirationDate = headers.getExpirationDate();
        if (expirationDate != null) {
            flexMessage.setTimeToLive(expirationDate - headers.getTimestamp());
        }
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            String key = header.getKey();
            if (!MessageHeaders.ID.equals(key) && !MessageHeaders.TIMESTAMP.equals(key) && !MessageHeaders.EXPIRATION_DATE.equals(key)) {
                flexMessage.setHeader(key, header.getValue());
            }
        }
        flexMessage.setDestination(this.getDestination().getId());
        MessageService messageService = (MessageService) getDestination().getService();
        messageService.pushMessageToClients(flexMessage, true);
        messageService.sendPushMessageFromPeer(flexMessage, true);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean handlesSubscriptions() {
        return true;
    }

    /**
     * Invoked when a Message is received from a Flex client.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(flex.messaging.messages.Message flexMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug("received Flex Message: " + flexMessage);
        }
        org.springframework.integration.core.Message<?> message = null;
        if (this.extractPayload) {
            Map headers = flexMessage.getHeaders();
            headers.put(FlexHeaders.CLIENT_ID, flexMessage.getClientId());
            headers.put(FlexHeaders.DESTINATION_ID, flexMessage.getDestination());
            long timestamp = flexMessage.getTimestamp();
            message = MessageBuilder.withPayload(flexMessage.getBody())
                    .copyHeaders(headers)
                    .setHeader(MessageHeaders.ID, flexMessage.getMessageId())
                    .setHeader(MessageHeaders.TIMESTAMP, timestamp)
                    .setExpirationDate(timestamp + flexMessage.getTimeToLive())
                    .build();
        }
        else {
            message = new GenericMessage<flex.messaging.messages.Message>(flexMessage);
        }
        this.messageChannel.send(message);
        return null;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Object manage(CommandMessage commandMessage) {
        String clientId = (String) commandMessage.getClientId();
        if (commandMessage.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
            this.subscriberIds.add(clientId);
            synchronized (this.consumerEndpoint) {
                if (!this.consumerEndpoint.isRunning()) {
                    this.consumerEndpoint.start();
                }
            }
            if (this.logger.isInfoEnabled()) {
                this.logger.info("client [" + clientId + "] subscribed to destination [" + this.getDestination().getId() + "]");
            }
        } else if (commandMessage.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            this.subscriberIds.remove(clientId);
            synchronized (this.consumerEndpoint) {
                if (this.subscriberIds.isEmpty() && this.consumerEndpoint.isRunning()) {
                    this.consumerEndpoint.stop();
                }
            }
            if (this.logger.isInfoEnabled()) {
                this.logger.info("client [" + clientId + "] unsubscribed from destination [" + this.getDestination().getId() + "]");
            }
        }
        return null;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setBeanName(String beanName) {
        this.setId(beanName);
    }

    /**
     * Sets the Spring Integration {@link MessageChannel} for sending and receiving messages
     * 
     * @param messageChannel the message channel
     */
    public void setMessageChannel(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
    }

}
