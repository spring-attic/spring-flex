package org.springframework.flex.config;

import org.springframework.beans.factory.FactoryBean;


public class NoDefaultConstructorFactoryBean implements FactoryBean<Object> {

    public NoDefaultConstructorFactoryBean(String foo) {
        
    }
    
    public Object getObject() throws Exception {
        return null;
    }

    public Class<?> getObjectType() {
        return null;
    }

    public boolean isSingleton() {
        return false;
    }

}
