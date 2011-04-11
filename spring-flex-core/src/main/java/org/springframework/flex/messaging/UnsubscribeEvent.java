package org.springframework.flex.messaging;

import org.springframework.context.ApplicationEvent;

public class UnsubscribeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final String clientId;
	
	private final String destinationId;
	
	public UnsubscribeEvent(Object source, String clientId, String destinationId) {
		super(source);
		this.clientId = clientId;
		this.destinationId = destinationId;
	}

	public String getClientId() {
		return clientId;
	}

	public String getDestinationId() {
		return destinationId;
	}
}
