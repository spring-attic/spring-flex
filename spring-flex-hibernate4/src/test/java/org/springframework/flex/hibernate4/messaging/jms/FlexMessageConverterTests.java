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

package org.springframework.flex.hibernate4.messaging.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.flex.messaging.jms.FlexMessageConverter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import flex.messaging.messages.AsyncMessage;

/**
 * @author Mark Fisher
 */
public class FlexMessageConverterTests {

    @Mock
    private Session session;

    @Test
    public void flexMessageToJmsMessageWithCustomConverter() throws Exception {
        FlexMessageConverter converter = new FlexMessageConverter(new CustomMessageConverter());
        AsyncMessage flexMessage = new AsyncMessage();
        flexMessage.setBody("foo");
        javax.jms.Message jmsMessage = converter.toMessage(flexMessage, this.session);
        assertEquals("foo", jmsMessage.getJMSMessageID());
    }

    @Test
    public void flexMessageToJmsTextMessage() throws Exception {
        FlexMessageConverter converter = new FlexMessageConverter();
        AsyncMessage flexMessage = new AsyncMessage();
        flexMessage.setBody("foo");
        javax.jms.Message jmsMessage = converter.toMessage(flexMessage, this.session);
        assertTrue(jmsMessage instanceof TextMessage);
        assertEquals("foo", ((TextMessage) jmsMessage).getText());
    }

    @Test
    public void flexMessageWithHeadersToJmsMessage() throws Exception {
        FlexMessageConverter converter = new FlexMessageConverter();
        AsyncMessage flexMessage = new AsyncMessage();
        flexMessage.setBody("foo");
        flexMessage.setHeader("name", "test");
        flexMessage.setHeader("num", 42);
        javax.jms.Message jmsMessage = converter.toMessage(flexMessage, this.session);
        assertTrue(jmsMessage instanceof TextMessage);
        assertEquals("foo", ((TextMessage) jmsMessage).getText());
        assertEquals("test", jmsMessage.getStringProperty("name"));
        assertEquals(42, jmsMessage.getIntProperty("num"));
    }

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.session.createTextMessage("foo")).thenReturn(new StubTextMessage("foo"));
    }

    @Test
    public void jmsMessageToFlexMessageWithCustomConverter() throws Exception {
        FlexMessageConverter converter = new FlexMessageConverter(new CustomMessageConverter());
        StubMessage jmsMessage = new StubMessage();
        jmsMessage.setJMSMessageID("ABC");
        Object result = converter.fromMessage(jmsMessage);
        assertTrue(result instanceof flex.messaging.messages.Message);
        flex.messaging.messages.Message flexMessage = (flex.messaging.messages.Message) result;
        assertEquals("ABC", flexMessage.getBody());
    }

    @Test
    public void jmsMessageWithPropertiesToFlexMessage() throws Exception {
        FlexMessageConverter converter = new FlexMessageConverter();
        javax.jms.Message jmsMessage = new StubMessage();
        jmsMessage.setStringProperty("foo", "bar");
        jmsMessage.setIntProperty("num", 42);
        Object result = converter.fromMessage(jmsMessage);
        assertTrue(result instanceof flex.messaging.messages.Message);
        flex.messaging.messages.Message flexMessage = (flex.messaging.messages.Message) result;
        assertEquals("bar", flexMessage.getHeader("foo"));
        assertEquals(42, flexMessage.getHeader("num"));
    }

    @Test
    public void jmsTextMessageToFlexMessage() throws Exception {
        FlexMessageConverter converter = new FlexMessageConverter();
        javax.jms.Message jmsMessage = new StubTextMessage("foo");
        Object result = converter.fromMessage(jmsMessage);
        assertTrue(result instanceof flex.messaging.messages.Message);
        flex.messaging.messages.Message flexMessage = (flex.messaging.messages.Message) result;
        assertEquals("foo", flexMessage.getBody());
    }

    private static class CustomMessageConverter implements MessageConverter {

        public Object fromMessage(Message message) throws JMSException, MessageConversionException {
            return message.getJMSMessageID();
        }

        public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
            Message message = new StubMessage();
            message.setJMSMessageID(object.toString());
            return message;
        }
    }

}
