package org.springframework.flex.core.io;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Embedded;
import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import flex.messaging.io.PropertyProxyRegistry;


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
	protected void registerPropertyProxy(SpringPropertyProxy proxy) {
		super.registerPropertyProxy(proxy);
		ReflectionUtils.doWithFields(proxy.getBeanType(), new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (PropertyProxyRegistry.getRegistry().getProxy(field.getType(), false, false) == null) {
					PropertyProxyRegistry.getRegistry().register(field.getType(), new SpringPropertyProxy(field.getType(), false));
				}
			}
		}, new EmbeddedFieldFilter());
	}
    
	private static final class EmbeddedFieldFilter implements FieldFilter {
		public boolean matches(Field field) {
			return field.getAnnotation(Embedded.class) != null;
		}
	}
}
