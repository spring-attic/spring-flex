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

package org.springframework.flex.config;

import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

/**
 * @author Jeremy Grelle
 * @author Sébastien Deleuze
 * @author Jose Barragan
 */
@ProfileValueSourceConfiguration(AbstractRuntimeEnvironmentAwareTests.RuntimeEnvironmentProfileValueSource.class)
public abstract class AbstractRuntimeEnvironmentAwareTests extends AbstractJUnit4SpringContextTests {

    protected static final String ENVIRONMENT = "environment";
    protected static final String BLAZEDS = "blazeds";
    protected static final String BLAZEDS_46 = "blazeds46";
    protected static final String LCDS = "lcds";
    protected static final String LCDS_LEGACY = "lcdslegacy";
	protected static final String HIBERNATE = "hibernate";

    public static final class RuntimeEnvironmentProfileValueSource implements ProfileValueSource {

        public String get(String key) {
           Assert.notNull(key, "Profile key cannot be null");
           Assert.isTrue(key.equals(ENVIRONMENT), "Unknown profile key.");
           if (RuntimeEnvironment.isBlazeDS()) {
               if (RuntimeEnvironment.isBlazeDS46()) {
                   return BLAZEDS_46;
               } else {
                   return BLAZEDS;
               }
           } else if (RuntimeEnvironment.isLCDS() && RuntimeEnvironment.isSpringSupportAvailable()) {
               return LCDS;
           } else if (RuntimeEnvironment.isLCDS()) {
        	   return LCDS_LEGACY;
           } else if (RuntimeEnvironment.isHibernateSupportAvailable() && RuntimeEnvironment.isSpringSupportAvailable()) {
	           return HIBERNATE;
           } else {
               throw new IllegalStateException("Runtime data services environment is unknown.");
           }
        }
        
    }

    public AbstractRuntimeEnvironmentAwareTests() {
        super();
    }

}
