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

package org.springframework.flex.core.io;

import java.lang.reflect.Method;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;


public class JpaHibernateConfigProcessor extends HibernateConfigProcessor{

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.hibernateConfigured) {
            for (EntityManagerFactory entityManagerFactory : BeanFactoryUtils.beansOfTypeIncludingAncestors(getBeanFactory(), EntityManagerFactory.class).values()) {
                setEntityManagerFactory(entityManagerFactory);
            }
        }
        super.afterPropertiesSet();
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Method gsfMethod = ReflectionUtils.findMethod(entityManagerFactory.getClass(), "getSessionFactory");
        Assert.notNull(gsfMethod, "Could not retrieve the underlying Hibernate SessionFactory from the provided EntityManagerFactory");
        setSessionFactory((SessionFactory) ReflectionUtils.invokeMethod(gsfMethod, entityManagerFactory));
    }
}
