package org.springframework.flex.messaging;

import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.util.UUIDUtils;

/**
 * Simple helper for sending Flex {@link AsyncMessage}s from a Java client.  The message will be routed
 * through the {@link MessageBroker} to the specified {@link MessageDestination}.  This allows for flexible
 * routing of the message using whatever {@link MessagingAdapter} is configured for the target destination,
 * be it the basic BlazeDS {@link ActionScriptAdapter}, one of the provided Spring {@link JmsAdapter} or {TODO SpringIntegrationAdapter},
 * or some other custom adapter implementation.
 * 
 * @author Jeremy Grelle
 */
public class MessageTemplate {

	private String defaultDestination;
	
	private MessageBroker messageBroker;
	
	private final String clientId = UUIDUtils.createUUID();
	
	private final AsyncMessageCreator defaultMessageCreator = new DefaultAsyncMessageCreator();
	
	public AsyncMessage createMessage() {
		return defaultMessageCreator.createMessage();
	}
	
	public void send(Object body) {
		send(defaultDestination, body);
	}
	
	public void send(String destination, Object body) {
		AsyncMessage message = defaultMessageCreator.createMessage();
		message.setDestination(destination);
		message.setBody(body);
		getMessageBroker().routeMessageToService(message, null);
	}
	
	public void send(AsyncMessageCreator creator) {
		getMessageBroker().routeMessageToService(creator.createMessage(), null);
	}
	
	public MessageBroker getMessageBroker() {
		if (messageBroker != null) {
			return messageBroker;
		}
		Assert.notNull(FlexContext.getMessageBroker(), "A MessageBroker was not set on the MessageTemplate " +
				"and no thread-local MessageBroker could be found in the current FlexContext.");
		return FlexContext.getMessageBroker();
	}
	
	public String getDefaultDestination() {
		return defaultDestination;
	}

	public void setDefaultDestination(String defaultDestination) {
		this.defaultDestination = defaultDestination;
	}

	public void setMessageBroker(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
	}
	
	private final class DefaultAsyncMessageCreator implements AsyncMessageCreator {
		public AsyncMessage createMessage() {
			AsyncMessage message = new AsyncMessage();
			message.setClientId(clientId);
			message.setMessageId(UUIDUtils.createUUID());
			message.setTimestamp(System.currentTimeMillis());
			return message;
		}
	}
}
