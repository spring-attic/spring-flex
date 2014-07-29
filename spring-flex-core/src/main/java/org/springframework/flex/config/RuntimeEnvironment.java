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

import org.springframework.util.ClassUtils;

import flex.messaging.config.ConfigurationFileResolver;

/**
 * Internal helper class to determine the type of runtime data services environment being used, to allow for
 * automatically adapting to the available capabilities.
 *
 * @author Rohit Kumar
 * @author Jeremy Grelle
 * @author Jose Barragan
 */
public abstract class RuntimeEnvironment {

    private static final String ASYNC_MESSAGE_BROKER_CLASS_NAME = "flex.messaging.AsyncMessageBroker";
    private static final String SPRING_SUPPORT_CLASS_NAME = "flex.springintegration.core.DataServicesConfigProcessor";
    private static final String HIBERNATE_SUPPORT_CLASS_NAME = "org.hibernate.Hibernate";

    private static final boolean IS_LCDS_ENVIRONMENT;
    private static final boolean IS_SPRING_SUPPORT_AVAILABLE;
    private static final boolean IS_BLAZEDS_46;
    private static final boolean IS_HIBERNATE_SUPPORT_AVAILABLE;

    static {
        boolean asyncMessageBrokerClassPresent;
        boolean springSupportClassPresent;
        boolean hibernateSupportClassPresent;

        try {
            ClassUtils.forName(ASYNC_MESSAGE_BROKER_CLASS_NAME, null);
            asyncMessageBrokerClassPresent = true;
        } catch (ClassNotFoundException ex) {
            asyncMessageBrokerClassPresent = false;
        }

        try {
            ClassUtils.forName(SPRING_SUPPORT_CLASS_NAME, null);
            springSupportClassPresent = true;
        } catch (ClassNotFoundException ex) {
            springSupportClassPresent = false;
        }

        try {
            ClassUtils.forName(HIBERNATE_SUPPORT_CLASS_NAME, null);
            hibernateSupportClassPresent = true;
        } catch (ClassNotFoundException ex) {
            hibernateSupportClassPresent = false;
        }

        IS_BLAZEDS_46 = ClassUtils.getMethodIfAvailable(ConfigurationFileResolver.class, "getFiles", String.class) != null;


        IS_LCDS_ENVIRONMENT = asyncMessageBrokerClassPresent;
        IS_SPRING_SUPPORT_AVAILABLE = springSupportClassPresent;
        IS_HIBERNATE_SUPPORT_AVAILABLE = hibernateSupportClassPresent;
    }

    /**
     * Returns <code>true</code> if the runtime data services environment is LCDS.
     */
    public static boolean isLCDS() {
        return IS_LCDS_ENVIRONMENT;
    }

    /**
     * Returns <code>true</code> if the runtime data services environment is BlazeDS.
     */
    public static boolean isBlazeDS() {
        return !IS_LCDS_ENVIRONMENT;
    }

    /**
     * Returns <code>true</code> if the runtime data services environment is BlazeDS 4.6 or greater.
     * @return
     */
    public static boolean isBlazeDS46(){
        return IS_BLAZEDS_46;
    }

    public static boolean isSpringSupportAvailable() {
        return IS_SPRING_SUPPORT_AVAILABLE;
    }

    public static boolean isHibernateSupportAvailable(){
        return IS_HIBERNATE_SUPPORT_AVAILABLE;
    }
}
