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
import org.springframework.web.context.support.GenericWebApplicationContext;

public abstract class AbstractFlexConfigurationTests extends AbstractSingleSpringContextTests {

    @Override
    protected ConfigurableApplicationContext createApplicationContext(String[] locations) {
        ConfigurableApplicationContext parentContext = createParentContext();

        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new WebInfResourceLoader(context)));
        prepareApplicationContext(context);
        customizeBeanFactory(context.getDefaultListableBeanFactory());
        createBeanDefinitionReader(context).loadBeanDefinitions(locations);
        context.setParent(parentContext);
        context.refresh();
        return context;
    }

    private ConfigurableApplicationContext createParentContext() {
        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext(new WebInfResourceLoader(context)));
        createBeanDefinitionReader(context).loadBeanDefinitions(new String[] { "classpath:org/springframework/flex/config/parent-context.xml" });
        context.refresh();
        return context;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "classpath:org/springframework/flex/config/message-broker.xml",
            "classpath:org/springframework/flex/config/remote-service.xml", "classpath:org/springframework/flex/config/remote-service-decorator.xml",
            "classpath:org/springframework/flex/config/message-destination.xml" };
    }
}
