/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.flex.hibernate3.config.xml;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.flex.config.xml.MessageBrokerBeanDefinitionParser;
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.hibernate3.config.AbstractFlexConfigurationTests;
import org.springframework.flex.hibernate3.config.MessageBrokerContextLoader;
import org.springframework.flex.hibernate3.config.TestWebInfResourceLoader;
import org.springframework.flex.hibernate3.core.io.domain.Person;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.MessageBroker;
import flex.messaging.io.PropertyProxyRegistry;

@ContextConfiguration(locations = "classpath:org/springframework/flex/hibernate3/config/message-broker.xml", loader = MessageBrokerBeanDefinitionParserTests.ParentContextLoader.class)
public class MessageBrokerBeanDefinitionParserTests extends AbstractFlexConfigurationTests {

	private static final String DATA_SERVICES_PROCESSOR_CLASS_NAME = (String) ReflectionTestUtils.getField(new MessageBrokerBeanDefinitionParser(), "DATASERVICES_PROCESSOR_CLASS_NAME");

	private MessageBroker broker;

	@Test
	@IfProfileValue(name = ENVIRONMENT, value = HIBERNATE)
	public void messageBrokerHibernateAutoConfigured() {
		Assert.assertTrue(PropertyProxyRegistry.getRegistry().getProxy(Person.class) instanceof SpringPropertyProxy);
	}

	public static final class ParentContextLoader extends MessageBrokerContextLoader {
		@Override
		protected ConfigurableApplicationContext createParentContext() {
			GenericWebApplicationContext context = new GenericWebApplicationContext();
			context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
			new XmlBeanDefinitionReader(context).loadBeanDefinitions(new String[]{"classpath:org/springframework/flex/hibernate3/core/io/hibernate-jpa-context.xml"});
			AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
			context.refresh();
			context.registerShutdownHook();
			return context;
		}
	}
}
