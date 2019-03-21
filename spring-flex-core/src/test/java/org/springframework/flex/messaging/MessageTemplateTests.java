/*
 * Copyright 2002-2009 the original author or authors.
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

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.flex.core.AbstractMessageBrokerTests;

import flex.messaging.MessageException;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.services.messaging.adapters.MessagingAdapter;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jeremy Grelle
 * @author Mark Fisher
 */
public class MessageTemplateTests extends AbstractMessageBrokerTests {

    private MessageTemplate template;

    private MessageDestinationFactory factory;

    private final AtomicReference<Message> messageHolder = new AtomicReference<Message>();

    @Before
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

    @After
    public void tearDown() throws Exception {
        this.factory.destroy();
        this.messageHolder.set(null);
    }

    @Test
    public void invalidDestination() throws Exception {

        this.template = new MessageTemplate();
        this.template.setMessageBroker(getMessageBroker());

        try {
            this.template.send("bogus", new Object());
            fail();
        } catch (MessageException ex) {
            // expected
        }
    }

    @Test
    public void noDestination() throws Exception {

        this.template = new MessageTemplate();
        this.template.setMessageBroker(getMessageBroker());

        try {
            this.template.send(new Object());
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void sendCustomMessage() throws Exception {
        final Object data = new Object();

        this.template = new MessageTemplate();
        this.template.setMessageBroker(getMessageBroker());
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

    @Test
    public void sendCustomMessageForDestination() throws Exception {
        final Object data = new Object();

        this.template = new MessageTemplate();
        this.template.setMessageBroker(getMessageBroker());
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

    @Test
    public void sendToDefaultDestination() throws Exception {
        Object data = new Object();

        this.template = new MessageTemplate();
        this.template.setMessageBroker(getMessageBroker());
        this.template.setDefaultDestination("test-destination");
        this.template.send(data);

        assertNotNull(this.messageHolder.get());
        assertNotNull(this.messageHolder.get().getBody());
        assertSame(data, this.messageHolder.get().getBody());
    }

    @Test
    public void sendToSpecifiedDestination() throws Exception {
        Object data = new Object();

        this.template = new MessageTemplate();
        this.template.setMessageBroker(getMessageBroker());
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
