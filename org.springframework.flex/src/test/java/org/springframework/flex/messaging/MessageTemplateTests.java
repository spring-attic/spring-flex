package org.springframework.flex.messaging;

import org.springframework.flex.core.AbstractMessageBrokerTests;

import flex.messaging.MessageException;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

public class MessageTemplateTests extends AbstractMessageBrokerTests {

	private MessageTemplate template;
	
	private SimpleMessageDestinationFactory factory;
	
	private TestMessagingAdapter adapter;
	
	public void setUp() throws Exception {
		adapter = new TestMessagingAdapter();
		adapter.setId("test-adapter");
		
		factory = new SimpleMessageDestinationFactory(adapter);
		factory.setMessageBroker(getMessageBroker());
		factory.setBeanName("test-destination");
		factory.afterPropertiesSet();
	}

	public void tearDown() throws Exception {
		factory.destroy();
	}
	
	public void testSendToDefaultDestination() {
		Object data = new Object();
		
		template = new MessageTemplate();
		template.setDefaultDestination("test-destination");
		template.send(data);
		
		assertNotNull(adapter.message.getBody());
		assertSame(data, adapter.message.getBody());
	}
	
	public void testSendToSpecifiedDestination() {
		Object data = new Object();
		
		template = new MessageTemplate();
		template.send("test-destination", data);
		
		assertNotNull(adapter.message.getBody());
		assertSame(data, adapter.message.getBody());
	}
	
	public void testSendCustomMessage() {
		final Object data = new Object();
		
		template = new MessageTemplate();
		template.send(new AsyncMessageCreator() {
			public AsyncMessage createMessage() {
				AsyncMessage message = template.createMessage();
				message.setBody(data);
				message.setDestination("test-destination");
				return message;
			}			
		});
		
		assertNotNull(adapter.message.getBody());
		assertSame(data, adapter.message.getBody());
	}
	
	public void testSendCustomMessageForDestination() {
		final Object data = new Object();
		
		template = new MessageTemplate();
		template.send(new AsyncMessageCreator() {
			public AsyncMessage createMessage() {
				AsyncMessage message = template.createMessageForDestination("test-destination");
				message.setBody(data);
				return message;
			}			
		});
		
		assertNotNull(adapter.message.getBody());
		assertSame(data, adapter.message.getBody());
	}
	
	public void testNoDestination() throws Exception{

		template = new MessageTemplate();
		
		try {
			template.send(new Object());
			fail();
		} catch (IllegalArgumentException ex) {
			//expected
		}
	}
	
	public void testInvalidDestination() throws Exception{
		
		template = new MessageTemplate();
		
		try {
			template.send("bogus", new Object());
			fail();
		} catch(MessageException ex) {
			//expected
		}
	}
	
	private static final class TestMessagingAdapter extends MessagingAdapter {

		protected Message message;
		
		@Override
		public Object invoke(Message message) {
			this.message = message;
			return null;
		}
	}

}
