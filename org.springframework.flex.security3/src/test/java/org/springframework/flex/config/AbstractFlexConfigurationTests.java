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

package org.springframework.flex.config;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.AbstractSingleSpringContextTests;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;

public abstract class AbstractFlexConfigurationTests extends AbstractSingleSpringContextTests {

    @Override
    protected ConfigurableApplicationContext createApplicationContext(String[] locations) {
        ConfigurableApplicationContext parentContext = createParentContext();

        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new TestWebInfResourceLoader(context)));
        prepareApplicationContext(context);
        customizeBeanFactory(context.getDefaultListableBeanFactory());
        
        locations = (String[]) ObjectUtils.addObjectToArray(locations, "classpath:org/springframework/flex/config/default-message-broker.xml");
        if (MessageBroker.getMessageBroker(BeanIds.MESSAGE_BROKER) != null) {
            FlexContext.clearThreadLocalObjects();
            MessageBroker.getMessageBroker(BeanIds.MESSAGE_BROKER).stop();
        }
        
        createBeanDefinitionReader(context).loadBeanDefinitions(locations);
        context.setParent(parentContext);
        context.refresh();
        return context;
    }

    protected ConfigurableApplicationContext createParentContext() {
        return null;
    }

    @Override
    protected abstract String[] getConfigLocations();
}
