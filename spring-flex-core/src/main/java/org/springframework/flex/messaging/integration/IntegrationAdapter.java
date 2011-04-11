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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.flex.messaging.SubscribeEvent;
import org.springframework.flex.messaging.UnsubscribeEvent;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.client.FlexClient;
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
public class IntegrationAdapter extends MessagingAdapter implements MessageHandler, InitializingBean, BeanNameAware, ApplicationEventPublisherAware {

    private final Log logger = LogFactory.getLog(getClass());

    private static final List<String> filteredHeaders;
    
    private volatile MessageChannel messageChannel;

    private volatile boolean extractPayload = true;
    
    private volatile boolean filterSender = true;
    
    private volatile ApplicationEventPublisher applicationEventPublisher;

    private final Set<Object> subscriberIds = new HashSet<Object>();
    
    private final Map<Object, String> clientSubscriptions = new HashMap<Object, String>();

    private volatile AbstractEndpoint consumerEndpoint;
    
    static {
    	filteredHeaders = new ArrayList<String>(FlexHeaders.ignored());
    	filteredHeaders.add(MessageHeaders.ID);
    	filteredHeaders.add(MessageHeaders.TIMESTAMP);
    	filteredHeaders.add(MessageHeaders.EXPIRATION_DATE);
    }

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
    public void handleMessage(Message<?> message) {
        if (logger.isDebugEnabled()) {
            logger.debug("received Integration Message: " + message);
        }
        AsyncMessage flexMessage = new AsyncMessage();
        flexMessage.setBody(message.getPayload());
        MessageHeaders headers = message.getHeaders();
        flexMessage.setMessageId(headers.containsKey(FlexHeaders.MESSAGE_ID) ? headers.get(FlexHeaders.MESSAGE_ID, String.class) : headers.getId().toString());
        Long timestamp = headers.containsKey(FlexHeaders.TIMESTAMP) ? Long.parseLong(headers.get(FlexHeaders.TIMESTAMP, String.class)) : headers.getTimestamp();
        flexMessage.setTimestamp(timestamp);
        Long expirationDate = headers.getExpirationDate();
        if (expirationDate != null) {
            flexMessage.setTimeToLive(expirationDate - timestamp);
        }
        if (headers.containsKey(FlexHeaders.MESSAGE_CLIENT_ID)) {
        	flexMessage.setClientId(headers.get(FlexHeaders.MESSAGE_CLIENT_ID));
        }
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            String key = header.getKey();
            if (!filteredHeaders.contains(key)) {
                flexMessage.setHeader(key, header.getValue());
            }
        }
        flexMessage.setDestination(this.getDestination().getId());
        MessageService messageService = (MessageService) getDestination().getService();
        if (filterSender && headers.containsKey(FlexHeaders.FLEX_CLIENT_ID)) {
        	Set<Object> subscribers = new HashSet<Object>(this.subscriberIds);
        	FlexClient flexClient = messageService.getMessageBroker().getFlexClientManager().getFlexClient(headers.get(FlexHeaders.FLEX_CLIENT_ID).toString());
        	for (Object subscriberId : this.subscriberIds) {
        		if (flexClient.getMessageClient(subscriberId.toString()) != null) {
        			subscribers.remove(subscriberId);
        		}
        	}
        	messageService.pushMessageToClients(subscribers, flexMessage, true);
        } else {
        	messageService.pushMessageToClients(flexMessage, true);
        }
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Object invoke(flex.messaging.messages.Message flexMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug("received Flex Message: " + flexMessage);
        }
        Message<?> message = null;
        if (this.extractPayload) {
            Map headers = flexMessage.getHeaders();
            headers.put(FlexHeaders.MESSAGE_CLIENT_ID, flexMessage.getClientId());
            headers.put(FlexHeaders.DESTINATION_ID, flexMessage.getDestination());
            headers.put(FlexHeaders.MESSAGE_ID, flexMessage.getMessageId());
            headers.put(FlexHeaders.TIMESTAMP, String.valueOf(flexMessage.getTimestamp()));
            if (FlexContext.getFlexClient() != null) {
                headers.put(FlexHeaders.FLEX_CLIENT_ID, FlexContext.getFlexClient().getId());
            }
            long timestamp = flexMessage.getTimestamp();
            message = MessageBuilder.withPayload(flexMessage.getBody())
                    .copyHeaders(headers)
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
            String flexClientId = FlexContext.getFlexClient().getId();
            this.clientSubscriptions.put(clientId, flexClientId);
            this.applicationEventPublisher.publishEvent(new SubscribeEvent(flexClientId, clientId, this.getDestination().getId()));
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
            String flexClientId = this.clientSubscriptions.remove(clientId);
            this.applicationEventPublisher.publishEvent(new UnsubscribeEvent(flexClientId, clientId, this.getDestination().getId()));
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

	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

}
