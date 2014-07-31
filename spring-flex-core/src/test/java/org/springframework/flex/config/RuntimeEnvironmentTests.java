/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.flex.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestExecutionListeners;


/**
 * Tests the RuntimeEnvironment class
 * 
 * @author Rohit Kumar
 * @author Jeremy Grelle
 */
@TestExecutionListeners(inheritListeners=false)
public class RuntimeEnvironmentTests extends AbstractRuntimeEnvironmentAwareTests {

    @Test
    @IfProfileValue(name=ENVIRONMENT, value=BLAZEDS)
    public void isLCDSInBlazeDSEnvironment() {
        assertFalse(RuntimeEnvironment.isLCDS());
    }

    @Test
    @IfProfileValue(name=ENVIRONMENT, value=BLAZEDS)
    public void isBlazeDSInBlazeDSEnvironment() {
        assertTrue(RuntimeEnvironment.isBlazeDS());
    }

    @Test
    @IfProfileValue(name=ENVIRONMENT, value=LCDS)
    public void isBlazeDSInLCDSEnvironment() {
        assertFalse(RuntimeEnvironment.isBlazeDS());
    }

    @Test
    @IfProfileValue(name=ENVIRONMENT, value=LCDS)
    public void isLCDSInLCDSEnvironment() {
        assertTrue(RuntimeEnvironment.isLCDS());
    }

    @Test
    public void isHibernate3SupportAvailable() {
        assertFalse(RuntimeEnvironment.isHibernate3SupportAvailable());
    }

    @Test
    public void isHibernate4SupportAvailable() {
        assertFalse(RuntimeEnvironment.isHibernate4SupportAvailable());
    }

    @Test
    public void isSpringFlexHibernate3SupportAvailable() {
        assertFalse(RuntimeEnvironment.isSpringFlexHibernate3SupportAvailable());
    }

    @Test
    public void isSpringFlexHibernate4SupportAvailable() {
        assertFalse(RuntimeEnvironment.isSpringFlexHibernate4SupportAvailable());
    }
}
