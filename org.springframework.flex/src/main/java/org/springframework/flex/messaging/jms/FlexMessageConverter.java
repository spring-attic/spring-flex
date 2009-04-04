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

package org.springframework.flex.messaging.jms;

import java.util.Enumeration;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.util.Assert;

import flex.messaging.messages.AsyncMessage;

/**
 * An implementation of the {@link MessageConverter} strategy interface that
 * passes Flex Message headers to JMS Message properties and vice versa.
 * <p>
 * This particular converter is only concerned with the passing of those
 * header/property values. It simply decorates a target converter to which
 * it delegates for the actual conversion between a JMS Message and a Flex
 * Message <em>body</em>. If no target converter is passed to the constructor,
 * then it will create and delegate to a {@link SimpleMessageConverter}
 * instance by default.
 * </p>
 * 
 * @author Mark Fisher
 */
public class FlexMessageConverter implements MessageConverter {

	private static final String HEADER_PREFIX = "spring_flex_";

	private static final String FLEX_CLIENT_ID = HEADER_PREFIX + "clientId";

	private static final String FLEX_TIME_TO_LIVE = HEADER_PREFIX + "timeToLive";


	private final Log logger = LogFactory.getLog(getClass());

	private final MessageConverter targetConverter;


	public FlexMessageConverter() {
		this(null);
	}

	public FlexMessageConverter(MessageConverter targetConverter) {
		this.targetConverter = (targetConverter != null) ? targetConverter : new SimpleMessageConverter();
	}


	public Object fromMessage(Message message) throws JMSException, MessageConversionException {
		Object messageBody = this.targetConverter.fromMessage(message);
		AsyncMessage flexMessage = new AsyncMessage();
		flexMessage.setBody(messageBody);
		flexMessage.setMessageId(message.getJMSMessageID());
		flexMessage.setClientId(message.getObjectProperty(FLEX_CLIENT_ID));
		flexMessage.setTimestamp(message.getJMSTimestamp());
		Object timeToLive = message.getObjectProperty(FLEX_TIME_TO_LIVE);
		if (timeToLive != null && long.class.isAssignableFrom(timeToLive.getClass())) {
			flexMessage.setTimeToLive(Long.parseLong(timeToLive.toString()));
		}
		Enumeration<?> propertyNames = message.getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String name = (String) propertyNames.nextElement();
			if (!name.startsWith(HEADER_PREFIX)) {
				flexMessage.setHeader(name, message.getObjectProperty(name));
			}
		}
		return flexMessage;
	}

	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
		Assert.state(object instanceof flex.messaging.messages.Message,
				"FlexMessageConverter can only handle Flex Messages");
		flex.messaging.messages.Message flexMessage = (flex.messaging.messages.Message) object;
		Object messageBody = flexMessage.getBody();
		Message jmsMessage = this.targetConverter.toMessage(messageBody, session);
		jmsMessage.setObjectProperty(FLEX_CLIENT_ID, flexMessage.getClientId());
		jmsMessage.setLongProperty(FLEX_TIME_TO_LIVE, flexMessage.getTimeToLive());
		Map<String, Object> headers = this.getFlexMessageHeaderMap(flexMessage);
		for (String key : headers.keySet()) {
			Object value = flexMessage.getHeader(key);
			try {
				jmsMessage.setObjectProperty(key, value);
			}
			catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("failed to copy Flex header '" + key + "'", e);
				}
			}
		}
		return jmsMessage;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getFlexMessageHeaderMap(flex.messaging.messages.Message flexMessage) {
		return (Map<String, Object>) flexMessage.getHeaders();
	}

}
