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

package org.springframework.flex.core;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ManageableComponentFactoryBeanTests {

    private ManageableComponentFactoryBean factoryBean;

    @Test
    public void componentCreationAndInitialization() throws Exception {
        this.factoryBean = new ManageableComponentFactoryBean(CustomManageableComponent.class);
        this.factoryBean.setBeanName("my-adapter");
        ManageableComponent component = (ManageableComponent) this.factoryBean.getObject();
        assertNotNull(component);
        assertTrue(((CustomManageableComponent) component).initialized);
        assertEquals("my-adapter", component.getId());
    }

    private static class CustomManageableComponent extends ManageableComponent {

        boolean initialized = false;

        public CustomManageableComponent() {
            super(false);
        }

        @Override
        public void initialize(String id, ConfigMap properties) {
            this.initialized = true;
        }

        @Override
        protected String getLogCategory() {
            return null;
        }
    }

}
