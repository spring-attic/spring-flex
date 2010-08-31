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

import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;
import org.springframework.util.Assert;

@ContextConfiguration(locations="classpath:org/springframework/flex/config/default-message-broker.xml", loader=MessageBrokerContextLoader.class)
@ProfileValueSourceConfiguration(AbstractFlexConfigurationTests.RuntimeEnvironmentProfileValueSource.class)
public abstract class AbstractFlexConfigurationTests extends AbstractJUnit38SpringContextTests {
    
    protected static final String ENVIRONMENT = "environment";
    
    protected static final String BLAZEDS = "blazeds";
    
    protected static final String LCDS = "lcds";
    
    public static final class RuntimeEnvironmentProfileValueSource implements ProfileValueSource {

        public String get(String key) {
           Assert.notNull(key, "Profile key cannot be null");
           if (RuntimeEnvironment.isBlazeDS()) {
               return BLAZEDS;
           } else if (RuntimeEnvironment.isLCDS()) {
               return LCDS;
           } else {
               throw new IllegalStateException("Runtime data services environment is unknown.");
           }
        }
        
    }
    
}
