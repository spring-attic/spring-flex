/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.flex.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.ClassUtils;

/**
 * Detects the version of Spring Security being used at runtime and returns an appropriate {@link SpringSecurityConfigHelper}
 *
 * @author Jeremy Grelle
 */
public class SpringSecurityConfigResolver {

    private static final String SECURITY3_CONFIG_HELPER_CLASSNAME = "org.springframework.flex.config.xml.SpringSecurity3ConfigHelper";
    
    public static SpringSecurityConfigHelper resolve() {
        return createConfigHelper(SECURITY3_CONFIG_HELPER_CLASSNAME);
    }
    
    static SpringSecurityConfigHelper createConfigHelper (String helperClassName) {
        try {
            return (SpringSecurityConfigHelper) ClassUtils.forName(helperClassName, SpringSecurityConfigResolver.class.getClassLoader()).newInstance();
        } catch (Exception ex) {
            throw new BeanCreationException("Could not construct an appropriate implementation of " + SpringSecurityConfigHelper.class.getName(), ex);
        }
    }
}
