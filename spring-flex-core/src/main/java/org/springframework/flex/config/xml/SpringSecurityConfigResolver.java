
package org.springframework.flex.config.xml;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.ClassUtils;

public class SpringSecurityConfigResolver {

    private static final String SECURITY3_AUTHENTICATION_CLASSNAME = "org.springframework.security.core.Authentication";
    
    private static final String SECURITY3_CONFIG_HELPER_CLASSNAME = "org.springframework.flex.config.xml.SpringSecurity3ConfigHelper";
    
    private static final String SECURITY2_CONFIG_HELPER_CLASSNAME = "org.springframework.flex.config.xml.SpringSecurity2ConfigHelper";
    
    static SpringSecurityConfigHelper resolve() {

        //try {
            //ClassUtils.forName(SECURITY3_AUTHENTICATION_CLASSNAME);
            return createConfigHelper(SECURITY3_CONFIG_HELPER_CLASSNAME);
        //} catch (ClassNotFoundException ex) {
            //return createConfigHelper(SECURITY2_CONFIG_HELPER_CLASSNAME);
        //}

    }
    
    static SpringSecurityConfigHelper createConfigHelper (String helperClassName) {
        try {
            return (SpringSecurityConfigHelper) ClassUtils.forName(helperClassName).newInstance();
        } catch (Exception ex) {
            throw new BeanCreationException("Could not construct an appropriate implementation of " + SpringSecurityConfigHelper.class.getName(), ex);
        }
    }
}
