
package org.springframework.flex.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.ClassUtils;

public class SpringSecurityConfigResolver {

    private static final String SECURITY3_CONFIG_HELPER_CLASSNAME = "org.springframework.flex.config.xml.SpringSecurity3ConfigHelper";
    
    static SpringSecurityConfigHelper resolve() {
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
