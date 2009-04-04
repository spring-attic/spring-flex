package org.springframework.flex.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.messaging.jms.JmsAdapter;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;
import flex.messaging.util.UUIDUtils;

/**
 * Simple helper for sending Flex {@link AsyncMessage}s from a Java client.  The message will be routed
 * through the {@link MessageBroker} to the specified {@link MessageDestination}.  This allows for flexible
 * routing of the message using whatever {@link MessagingAdapter} is configured for the target destination,
 * be it the basic BlazeDS {@link ActionScriptAdapter}, one of the provided Spring adapters such as {@link JmsAdapter}
 * or {TODO SpringIntegrationAdapter}, or some other custom adapter implementation.
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
	
	public AsyncMessage createMessage() {
		return defaultMessageCreator.createMessage();
	}
	
	public AsyncMessage createMessageForDestination(String destination) {
		AsyncMessage message = defaultMessageCreator.createMessage();
		message.setDestination(destination);
		return message;
	}
	
	public void send(Object body) {
		Assert.hasText(defaultDestination, "Cannot send message - no default destination has been set for this MessageTemplate.");
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
				"and no thread-local MessageBroker could be found in the FlexContext.");
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

	public void afterPropertiesSet() throws Exception {
		if (messageBroker == null) {
			//first try the default id
			if (beanFactory.containsBean(BeanIds.MESSAGE_BROKER)) {
				messageBroker = (MessageBroker) beanFactory.getBean(BeanIds.MESSAGE_BROKER, MessageBroker.class);
			} else if (beanFactory instanceof ListableBeanFactory) {
				ListableBeanFactory lbf = (ListableBeanFactory) beanFactory;
				String[] brokerNames = lbf.getBeanNamesForType(MessageBroker.class);
				if (brokerNames.length == 1) {
					messageBroker = (MessageBroker) lbf.getBean(brokerNames[0]);
				} else if (brokerNames.length > 1){
					log.warn("A MessageBroker was not explicitly set and one could not be auto-detected in the MessageTemplate's bean factory as " +
							"multiple MessageBrokers were found.  Will fall back to attempting to obtain the MessageBroker from the FlexContext for " +
							"each operation.");
				} else {
					log.warn("A MessageBroker was not explicitly set and one could not be found in the MessageTemplate's bean factory.  Will " +
						"will fall back to attempting to obtain the MessageBroker from the FlexContext for each operation.");
				}
			}
		}	
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;		
	}
}
