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

package org.springframework.flex.messaging;

import org.springframework.flex.core.AbstractDestinationFactory;
import org.springframework.flex.core.MessageBrokerFactoryBean;
import org.springframework.util.Assert;

import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * A factory for Flex MessageDestinations that can be configured with a
 * Spring-managed MessagingAdapter instance.
 * 
 * <p>
 * The destination will be exposed to the Flex client as a BlazeDS
 * {@link MessageDestination}. By default, the id of the destination will be
 * the same as the bean name of this exporter. This may be overridden using the
 * {@link #setDestinationId(String) 'destinationId'} property.
 * </p>
 * 
 * @see MessageBrokerFactoryBean
 * 
 * @author Mark Fisher
 */
public class MessageDestinationFactory extends AbstractDestinationFactory {

	private final MessagingAdapter adapter;


	public MessageDestinationFactory() {
		this(new ActionScriptAdapter());
	}

	public MessageDestinationFactory(MessagingAdapter adapter) {
		Assert.notNull(adapter, "adapter must not be null");
		this.adapter = adapter;
	}


	@Override
	protected Destination createDestination(String destinationId, MessageBroker broker) throws Exception {
		MessageService messageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
		Assert.notNull(messageService, "Could not find a proper MessageService in the Flex MessageBroker.");
		MessageDestination destination = (MessageDestination) messageService.createDestination(destinationId);
		destination.setAdapter(this.adapter);
		return destination;
	}

	@Override
	protected void initializeDestination(Destination destination) {
		destination.start();
		destination.getAdapter().start();
	}

	@Override
	protected void destroyDestination(String destinationId, MessageBroker broker) {
		MessageService messageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
		if (messageService == null) {
			return;
		}
		messageService.removeDestination(destinationId);
	}

}
