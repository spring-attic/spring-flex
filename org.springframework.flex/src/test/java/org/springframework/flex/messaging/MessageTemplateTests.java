/*
 * Copyright 2008-2009 the original author or authors.
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

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.flex.core.AbstractMessageBrokerTests;

import flex.messaging.MessageException;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * @author Jeremy Grelle
 * @author Mark Fisher
 */
public class MessageTemplateTests extends AbstractMessageBrokerTests {

	private MessageTemplate template;

	private MessageDestinationFactory factory;

	private final AtomicReference<Message> messageHolder = new AtomicReference<Message>();


	public void setUp() throws Exception {
		this.messageHolder.set(null);

		StaticApplicationContext context = new StaticApplicationContext();
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("messageHolder", this.messageHolder);
		context.registerPrototype("test-adapter", TestMessagingAdapter.class, mpvs);

		factory = new MessageDestinationFactory();
		factory.setServiceAdapter("test-adapter");
		factory.setBeanFactory(context);
		factory.setMessageBroker(getMessageBroker());
		factory.setBeanName("test-destination");
		factory.afterPropertiesSet();
	}

	public void tearDown() throws Exception {
		factory.destroy();
		messageHolder.set(null);
	}


	public void testSendToDefaultDestination() {
		Object data = new Object();
		
		template = new MessageTemplate();
		template.setDefaultDestination("test-destination");
		template.send(data);
		
		assertNotNull(messageHolder.get());
		assertNotNull(messageHolder.get().getBody());
		assertSame(data, messageHolder.get().getBody());
	}

	public void testSendToSpecifiedDestination() {
		Object data = new Object();
		
		template = new MessageTemplate();
		template.send("test-destination", data);
		
		assertNotNull(messageHolder.get());
		assertNotNull(messageHolder.get().getBody());
		assertSame(data, messageHolder.get().getBody());
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
		
		assertNotNull(messageHolder.get());
		assertNotNull(messageHolder.get().getBody());
		assertSame(data, messageHolder.get().getBody());
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
		
		assertNotNull(messageHolder.get());
		assertNotNull(messageHolder.get().getBody());
		assertSame(data, messageHolder.get().getBody());
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


	static class TestMessagingAdapter extends MessagingAdapter {

		private AtomicReference<Message> messageHolder;

		public void setMessageHolder(AtomicReference<Message> messageHolder) {
			this.messageHolder = messageHolder;
		}

		@Override
		public Object invoke(Message message) {
			this.messageHolder.set(message);
			return null;
		}
	}

}
