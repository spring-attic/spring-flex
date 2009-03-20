/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.flex.messaging.jms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import flex.messaging.MessageClient;
import flex.messaging.MessageClientListener;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * @author Mark Fisher
 */
class JmsAdapter extends MessagingAdapter implements MessageClientListener {

	private final Log logger = LogFactory.getLog(getClass());

	private final JmsTemplate jmsTemplate;

	private final DefaultMessageListenerContainer messageListenerContainer;

	private final Set<Object> subscriberIds = new HashSet<Object>();

	private final Map<Object, MessageClient> clientMap = new HashMap<Object, MessageClient>(); 


	JmsAdapter(String id, JmsTemplate jmsTemplate) {
		this.setId(id);
		FlexMessageConverter flexMessageConverter =
				new FlexMessageConverter(jmsTemplate.getMessageConverter());
		jmsTemplate.setMessageConverter(flexMessageConverter);
		this.jmsTemplate = jmsTemplate;
		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
		messageListenerAdapter.setMessageConverter(flexMessageConverter);
		messageListenerAdapter.setDelegate(this);
		DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
		messageListenerContainer.setConnectionFactory(jmsTemplate.getConnectionFactory());
		messageListenerContainer.setDestination(jmsTemplate.getDefaultDestination());
		messageListenerContainer.setMessageListener(messageListenerAdapter);
		messageListenerContainer.setAutoStartup(false);
		this.messageListenerContainer = messageListenerContainer;
	}


	@Override
	public void start() {
		super.start();
		MessageClient.addMessageClientCreatedListener(this);
		this.messageListenerContainer.afterPropertiesSet();
	}

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
	 * Invoked when a Message is received from a JMS client.
	 */
	void handleMessage(Message flexMessage) {
		flexMessage.setDestination(this.getDestination().getId());
		MessageService messageService = ((MessageService) getDestination().getService());
		// NOTE: for now the message is sent to all subscribers and no selectors are evaluated
		messageService.serviceMessageFromAdapter(flexMessage, true);
	}

	@Override
	public Object manage(CommandMessage commandMessage) {
		String clientId = (String) commandMessage.getClientId();
		if (commandMessage.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
			this.subscriberIds.add(clientId);
			synchronized (this.messageListenerContainer) {
				if (!this.messageListenerContainer.isRunning()) {
					this.messageListenerContainer.start();
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("client [" +clientId +"] subscribed to destination [" + this.getDestination().getId() + "]");
			}
		}
		else if (commandMessage.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
			this.subscriberIds.remove(clientId);
			synchronized (this.messageListenerContainer) {
				if (this.subscriberIds.isEmpty() && this.messageListenerContainer.isRunning()) {
					this.messageListenerContainer.stop();
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("client [" +clientId +"] unsubscribed from destination [" + this.getDestination().getId() + "]");
			}
		}
		return null;
	}


	/*
	 * MessageClientListener implementation
	 */

	public void messageClientCreated(MessageClient messageClient) {
		messageClient.addMessageClientDestroyedListener(this);
		this.clientMap.put(messageClient.getClientId(), messageClient);
	}

	public void messageClientDestroyed(MessageClient messageClient) {
		this.clientMap.remove(messageClient.getClientId());
	}

}
