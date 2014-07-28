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

package org.springframework.flex.hibernate3.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.web.context.support.StaticWebApplicationContext;

import flex.messaging.config.ApacheXPathServerConfigurationParser;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.config.XPathServerConfigurationParser;

@TestExecutionListeners(value = {}, inheritListeners = false)
public class FlexConfigurationManagerTests extends AbstractRuntimeEnvironmentAwareTests {

	ServletConfig config = new MockServletConfig();

	StaticWebApplicationContext context = new StaticWebApplicationContext();

	ConfigurationManager configManager;

	private static final Log log = LogFactory.getLog(FlexConfigurationManagerTests.class);

	@Before
	public void setUp() {
		this.context.setServletConfig(this.config);
	}

	@Test
	public void customConfiguration() {
		this.context.registerSingleton("configParser", XPathServerConfigurationParser.class);
		RuntimeBeanReference parserReference = new RuntimeBeanReference("configParser");
		GenericBeanDefinition configManagerDef = new GenericBeanDefinition();
		configManagerDef.setBeanClass(FlexConfigurationManager.class);
		configManagerDef.getPropertyValues().addPropertyValue("configurationParser", parserReference);
		configManagerDef.getPropertyValues().addPropertyValue("configurationPath", "classpath:org/springframework/flex/hibernate3/core/services-config.xml");
		this.context.getDefaultListableBeanFactory().registerBeanDefinition("configurationManager", configManagerDef);
		this.context.refresh();

		this.configManager = (ConfigurationManager) this.context.getBean("configurationManager");

		MessagingConfiguration messagingConfiguration = this.configManager.getMessagingConfiguration(this.config);

		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
	}

	@Test
	public void getMessagingConfiguration() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath:org/springframework/flex/hibernate3/core/services-config.xml");

		MessagingConfiguration messagingConfiguration = this.configManager.getMessagingConfiguration(this.config);

		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
	}

	@Test
	public void parserPerformance() {
		long start = 0;
		long end = 0;
		long defaultTotal = 0;
		long java5Total = 0;
		long xalanTotal = 0;
		int iterations = 5;

		this.configManager = new FlexConfigurationManager(this.context, "classpath:org/springframework/flex/hibernate3/core/services-config.xml");

		start = System.currentTimeMillis();
		for (int x = 0; x < iterations; x++) {
			this.configManager.getMessagingConfiguration(this.config);
		}
		end = System.currentTimeMillis();
		defaultTotal = end - start;

		((FlexConfigurationManager) this.configManager).setConfigurationParser(new XPathServerConfigurationParser());

		start = System.currentTimeMillis();
		for (int x = 0; x < iterations; x++) {
			this.configManager.getMessagingConfiguration(this.config);
		}
		end = System.currentTimeMillis();
		java5Total = end - start;

		((FlexConfigurationManager) this.configManager).setConfigurationParser(new ApacheXPathServerConfigurationParser());

		start = System.currentTimeMillis();
		for (int x = 0; x < iterations; x++) {
			this.configManager.getMessagingConfiguration(this.config);
		}
		end = System.currentTimeMillis();
		xalanTotal = end - start;

		log.info("Default total parser time = " + defaultTotal + "ms");
		log.info("Java 5 total parser time = " + java5Total + "ms");
		log.info("Xalan total parser time = " + xalanTotal + "ms");

	}

	@Test
	public void doesNotExist() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath:org/springframework/flex/hibernate3/core/foo.xml");

		try {
			this.configManager.getMessagingConfiguration(this.config);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void classpathResourcePattern() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath*:org/springframework/flex/hibernate3/core/services-config.xml");

		MessagingConfiguration messagingConfiguration = this.configManager.getMessagingConfiguration(this.config);

		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
	}

	@Test
	public void classpathResourcePatternDoesNotExist() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath*:org/springframework/flex/hibernate3/core/foo.xml");

		try {
			this.configManager.getMessagingConfiguration(this.config);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void classpathResourcePatternTooAmbiguous() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath*:org/springframework/flex/hibernate3/core/*-config.xml");

		try {
			this.configManager.getMessagingConfiguration(this.config);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void nullServletConfig() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath:org/springframework/flex/hibernate3/core/services-config.xml");

		try {
			this.configManager.getMessagingConfiguration(null);
			fail();
		} catch (Exception ex) {
			// expected
		}
	}

	@Test
	@IfProfileValue(name = ENVIRONMENT, value = BLAZEDS_46)
	public void includedChannelsFiles() {
		this.configManager = new FlexConfigurationManager(this.context, "classpath:org/springframework/flex/hibernate3/core/services-config-4.6.xml");

		MessagingConfiguration messagingConfiguration = this.configManager.getMessagingConfiguration(this.config);

		assertNotNull(messagingConfiguration);
		assertNotNull(messagingConfiguration.getServiceSettings("message-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("proxy-service"));
		assertNotNull(messagingConfiguration.getServiceSettings("remoting-service"));
		assertNotNull(messagingConfiguration.getChannelSettings("my-included-amf"));
		assertNotNull(messagingConfiguration.getChannelSettings("my-dir-included-amf-1"));
		assertNotNull(messagingConfiguration.getChannelSettings("my-dir-included-amf-2"));
	}
}
