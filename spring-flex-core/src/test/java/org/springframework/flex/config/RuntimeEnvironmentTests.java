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

package org.springframework.flex.config;

import junit.framework.TestCase;

/**
 * Tests the RuntimeEnvironment class
 * 
 * @author Rohit Kumar
 */
public class RuntimeEnvironmentTests extends TestCase {

    /**
     * Tests {@link org.springframework.flex.config.RuntimeEnvironment#isLCDS()}.
     */
    public void testIsLCDSInBlazeDSEnvironment() {
        assertFalse(RuntimeEnvironment.isLCDS());
    }

    /**
     * Tests {@link org.springframework.flex.config.RuntimeEnvironment#isBlazeDS()}.
     */
    public void testIsBlazeDSInBlazeDSEnvironment() {
        assertTrue(RuntimeEnvironment.isBlazeDS());
    }
    
}
