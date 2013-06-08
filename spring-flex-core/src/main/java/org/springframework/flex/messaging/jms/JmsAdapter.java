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

package org.springframework.flex.messaging.jms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.flex.messaging.SubscribeEvent;
import org.springframework.flex.messaging.UnsubscribeEvent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * A {@link MessagingAdapter} implementation that enables sending and receiving messages via JMS.
 * 
 * @author Mark Fisher
 * @author Jeremy Grelle
 */
public class JmsAdapter extends MessagingAdapter implements InitializingBean, BeanNameAware, ApplicationEventPublisherAware {

    private final Log logger = LogFactory.getLog(getClass());

    private volatile ConnectionFactory connectionFactory;

    private volatile Object destination;

    private volatile boolean pubSubDomain;

    private volatile MessageConverter messageConverter;
    
    private volatile ApplicationEventPublisher applicationEventPublisher;

    private final JmsTemplate jmsTemplate = new JmsTemplate();

    private final DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();

    private final Set<Object> subscriberIds = new HashSet<Object>();
    
    private final Map<Object, String> clientSubscriptions = new HashMap<Object, String>();

    /**
     * 
     * {@inheritDoc}
     */
    public void afterPropertiesSet() {
        Assert.notNull(this.connectionFactory, "connectionFactory is required");
        Assert.notNull(this.destination, "destination or destination name is required");
        this.jmsTemplate.setConnectionFactory(this.connectionFactory);
        MessageConverter converterToSet = this.messageConverter;
        if (converterToSet == null || !(converterToSet instanceof FlexMessageConverter)) {
            converterToSet = new FlexMessageConverter(converterToSet);
        }
        this.jmsTemplate.setMessageConverter(converterToSet);
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
        messageListenerAdapter.setMessageConverter(converterToSet);
        messageListenerAdapter.setDelegate(this);
        this.messageListenerContainer.setConnectionFactory(this.connectionFactory);
        this.messageListenerContainer.setMessageListener(messageListenerAdapter);
        this.messageListenerContainer.setAutoStartup(false);
        if (this.destination instanceof Destination) {
            this.jmsTemplate.setDefaultDestination((Destination) this.destination);
            this.messageListenerContainer.setDestination((Destination) this.destination);
        } else {
            this.jmsTemplate.setPubSubDomain(this.pubSubDomain);
            this.jmsTemplate.setDefaultDestinationName((String) this.destination);
            this.messageListenerContainer.setPubSubDomain(this.pubSubDomain);
            this.messageListenerContainer.setDestinationName((String) this.destination);
        }
        this.jmsTemplate.afterPropertiesSet();
        this.messageListenerContainer.afterPropertiesSet();
    }

    /**
     * Returns the {@link JmsTemplate} used by this adapter
     * 
     * @return the jms template
     */
    public JmsTemplate getJmsTemplate() {
        return this.jmsTemplate;
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
    @Override
    public Object invoke(Message flexMessage) {
        this.jmsTemplate.convertAndSend(flexMessage);
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
            synchronized (this.messageListenerContainer) {
                if (!this.messageListenerContainer.isActive()) {
                    this.messageListenerContainer.initialize();
                }
                if (!this.messageListenerContainer.isRunning()) {
                    this.messageListenerContainer.start();
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
            synchronized (this.messageListenerContainer) {
                if (this.subscriberIds.isEmpty() && this.messageListenerContainer.isActive()){ 
                    this.messageListenerContainer.shutdown();
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
     * Sets the {@link ConnectionFactory} to use for sending and receiving JMS messages
     * 
     * @param connectionFactory the connection factory
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Sets the {@link DestinationResolver} for resolving the JMS destination for this adapter
     * 
     * @param destinationResolver the destination resolver
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        Assert.notNull(destinationResolver, "destinationResolver must not be null");
        this.jmsTemplate.setDestinationResolver(destinationResolver);
        this.messageListenerContainer.setDestinationResolver(destinationResolver);
    }

    /**
     * Sets the JMS {@link Destination} for messages sent and received by this adapter
     * 
     * @param destination the destination
     */
    public void setJmsDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * Sets the {@link MessageConverter} for messages sent and received by this adapter.
     * 
     * @param messageConverter the message converter
     */
    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the JMS queue name for messages sent and received by this adapter.
     * 
     * @param queueName the JMS queue name
     */
    public void setQueueName(String queueName) {
        this.destination = queueName;
    }

    /**
     * Sets the JMS topic name for messages sent and received by this adapter.
     * 
     * @param topicName the JMS topic name
     */
    public void setTopicName(String topicName) {
        this.pubSubDomain = true;
        this.destination = topicName;
    }

    /**
     * Sets the {@link PlatformTransactionManager} to be used when sending and receiving messages
     * 
     * @param transactionManager the transaction manager
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        Assert.notNull(transactionManager, "transactionManager must not be null");
        this.messageListenerContainer.setTransactionManager(transactionManager);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (!this.messageListenerContainer.isActive()) {
            this.messageListenerContainer.initialize();
        }
        super.start();
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        this.messageListenerContainer.shutdown();
        super.stop();
    }

    public DefaultMessageListenerContainer getMessageListenerContainer() {
        return this.messageListenerContainer;
    }

    /**
     * Invoked when a Message is received from a JMS client.
     */
    void handleMessage(Message flexMessage) {
        flexMessage.setDestination(this.getDestination().getId());
        MessageService messageService = (MessageService) getDestination().getService();
        messageService.pushMessageToClients(flexMessage, true);
        messageService.sendPushMessageFromPeer(flexMessage, true);
    }

	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

}
