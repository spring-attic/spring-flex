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

import org.springframework.context.ApplicationEvent;

import flex.messaging.MessageDestination;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * Event that will be broadcast whenever a Flex client subscribes to a {@link MessageDestination} that is using 
 * one of the Spring-provided {@link MessagingAdapter MessagingAdapters}.
 *
 * @author Jeremy Grelle
 */
public class SubscribeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final String clientId;
	
	private final String destinationId;
	
	public SubscribeEvent(Object source, String clientId, String destinationId) {
		super(source);
		this.clientId = clientId;
		this.destinationId = destinationId;
	}

	/**
	 * The client id of the subscribed Flex client.
	 */
	public String getClientId() {
		return clientId;
	}
	
	/**
	 * The id of the message destination.
	 */
	public String getDestinationId() {
		return destinationId;
	}
}
