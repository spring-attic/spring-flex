package org.springframework.flex.messaging;

import org.springframework.flex.core.AbstractDestinationExporter;
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
public class MessagingDestinationExporter extends AbstractDestinationExporter {

	private final MessagingAdapter adapter;


	public MessagingDestinationExporter() {
		this(new ActionScriptAdapter());
	}

	public MessagingDestinationExporter(MessagingAdapter adapter) {
		this.adapter = adapter;
	}


	@Override
	protected Destination createDestination(String destinationId, MessageBroker broker) throws Exception {
		MessageService messageService = (MessageService) broker.getServiceByType(MessageService.class.getName());
		Assert.notNull(messageService, "Could not find a proper MessageService in the Flex MessageBroker.");
		MessageDestination destination = (MessageDestination) messageService.createDestination(destinationId);
		if (this.adapter != null) {
			destination.setAdapter(this.adapter);
		}
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
