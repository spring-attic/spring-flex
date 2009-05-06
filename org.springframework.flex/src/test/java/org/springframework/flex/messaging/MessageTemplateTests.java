/*
 * Copyright 2002-2009 the original author or authors.
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

    @Override
    public void setUp() throws Exception {
        this.messageHolder.set(null);

        StaticApplicationContext context = new StaticApplicationContext();
        MutablePropertyValues mpvs = new MutablePropertyValues();
        mpvs.addPropertyValue("messageHolder", this.messageHolder);
        context.registerPrototype("test-adapter", TestMessagingAdapter.class, mpvs);

        this.factory = new MessageDestinationFactory();
        this.factory.setServiceAdapter("test-adapter");
        this.factory.setBeanFactory(context);
        this.factory.setMessageBroker(getMessageBroker());
        this.factory.setBeanName("test-destination");
        this.factory.afterPropertiesSet();
    }

    @Override
    public void tearDown() throws Exception {
        this.factory.destroy();
        this.messageHolder.set(null);
    }

    public void testInvalidDestination() throws Exception {

        this.template = new MessageTemplate();

        try {
            this.template.send("bogus", new Object());
            fail();
        } catch (MessageException ex) {
            // expected
        }
    }

    public void testNoDestination() throws Exception {

        this.template = new MessageTemplate();

        try {
            this.template.send(new Object());
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testSendCustomMessage() {
        final Object data = new Object();

        this.template = new MessageTemplate();
        this.template.send(new AsyncMessageCreator() {

            public AsyncMessage createMessage() {
                AsyncMessage message = MessageTemplateTests.this.template.createMessage();
                message.setBody(data);
                message.setDestination("test-destination");
                return message;
            }
        });

        assertNotNull(this.messageHolder.get());
        assertNotNull(this.messageHolder.get().getBody());
        assertSame(data, this.messageHolder.get().getBody());
    }

    public void testSendCustomMessageForDestination() {
        final Object data = new Object();

        this.template = new MessageTemplate();
        this.template.send(new AsyncMessageCreator() {

            public AsyncMessage createMessage() {
                AsyncMessage message = MessageTemplateTests.this.template.createMessageForDestination("test-destination");
                message.setBody(data);
                return message;
            }
        });

        assertNotNull(this.messageHolder.get());
        assertNotNull(this.messageHolder.get().getBody());
        assertSame(data, this.messageHolder.get().getBody());
    }

    public void testSendToDefaultDestination() {
        Object data = new Object();

        this.template = new MessageTemplate();
        this.template.setDefaultDestination("test-destination");
        this.template.send(data);

        assertNotNull(this.messageHolder.get());
        assertNotNull(this.messageHolder.get().getBody());
        assertSame(data, this.messageHolder.get().getBody());
    }

    public void testSendToSpecifiedDestination() {
        Object data = new Object();

        this.template = new MessageTemplate();
        this.template.send("test-destination", data);

        assertNotNull(this.messageHolder.get());
        assertNotNull(this.messageHolder.get().getBody());
        assertSame(data, this.messageHolder.get().getBody());
    }

    static class TestMessagingAdapter extends MessagingAdapter {

        private AtomicReference<Message> messageHolder;

        @Override
        public Object invoke(Message message) {
            this.messageHolder.set(message);
            return null;
        }

        public void setMessageHolder(AtomicReference<Message> messageHolder) {
            this.messageHolder = messageHolder;
        }
    }

}
