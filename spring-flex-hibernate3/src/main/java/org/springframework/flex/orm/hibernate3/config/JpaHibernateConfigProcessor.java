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

package org.springframework.flex.orm.hibernate3.config;

import java.lang.reflect.Method;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Specialized subclass of {@link HibernateConfigProcessor} that can be used with a Hibernate-provided JPA {@link EntityManagerFactory} 
 * instead of the native Hibernate {@link SessionFactory}.
 * 
 * @see HibernateConfigProcessor
 * @author Jeremy Grelle
 */
public class JpaHibernateConfigProcessor extends HibernateConfigProcessor{

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.hibernateConfigured) {
            for (EntityManagerFactory entityManagerFactory : BeanFactoryUtils.beansOfTypeIncludingAncestors(getBeanFactory(), EntityManagerFactory.class).values()) {
                setEntityManagerFactory(entityManagerFactory);
            }
        }
        super.afterPropertiesSet();
    }

    /**
     * Sets the Hibernate-provided {@link EntityManagerFactory} to be used for reading type metadata.  If this property is not 
     * explicitly set, all available {@code EntityManagerFactory} instances will be retrieved from the containing 
     * {@link BeanFactory} and have their type metadata extracted for use in {@link #findTypesToRegister()}
     * @param entityManagerFactory the entity manager factory from which to read metadata
     */
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Method gsfMethod = ReflectionUtils.findMethod(entityManagerFactory.getClass(), "getSessionFactory");
        Assert.notNull(gsfMethod, "Could not retrieve the underlying Hibernate SessionFactory from the provided EntityManagerFactory");
        setSessionFactory((SessionFactory) ReflectionUtils.invokeMethod(gsfMethod, entityManagerFactory));
    }
}
