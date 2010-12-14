package org.springframework.flex.core.io;

import java.lang.reflect.Method;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;


public class JpaHibernateConfigProcessor extends HibernateConfigProcessor{

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getSessionFactory() == null) {
            if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getBeanFactory(), EntityManagerFactory.class).length > 0) {
                setEntityManagerFactory(BeanFactoryUtils.beanOfTypeIncludingAncestors(getBeanFactory(), EntityManagerFactory.class));
            }
        }
        super.afterPropertiesSet();
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Method gsfMethod = ReflectionUtils.findMethod(entityManagerFactory.getClass(), "getSessionFactory");
        Assert.notNull(gsfMethod, "Could not retrieve the underlying Hibernate SessionFactory from the provided EntityManagerFactory");
        setSessionFactory((SessionFactory) ReflectionUtils.invokeMethod(gsfMethod, entityManagerFactory));
    }
    
    @Override
    protected ConversionService getDefaultConversionService() {
        GenericConversionService conversionService = (GenericConversionService) super.getDefaultConversionService();
        conversionService.addConverter(new JpaNumericVersionConverter());
        return conversionService;
    }
}
