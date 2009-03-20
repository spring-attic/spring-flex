/*
 * Copyright 2002-2009 the original author or authors.
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

import org.springframework.flex.messaging.AbstractMessagingDestinationExporter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

import flex.messaging.services.messaging.adapters.MessagingAdapter;

/**
 * @author Mark Fisher
 */
public class JmsDestinationExporter extends AbstractMessagingDestinationExporter {

	private volatile JmsTemplate jmsTemplate;


	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}


	@Override
	protected MessagingAdapter createAdapter() throws Exception {
		Assert.notNull(jmsTemplate, "jmsTemplate must not be null");
		JmsAdapter adapter = new JmsAdapter(this.getDestinationId() + "-adapter", this.jmsTemplate);
		return adapter;
	}

}
