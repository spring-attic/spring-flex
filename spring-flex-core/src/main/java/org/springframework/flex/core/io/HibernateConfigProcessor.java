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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.util.Assert;


public class HibernateConfigProcessor extends AbstractAmfConversionServiceConfigProcessor implements BeanFactoryAware, InitializingBean {

    private Log log = LogFactory.getLog(HibernateConfigProcessor.class);
    
    private Set<ClassMetadata> classMetadata = new HashSet<ClassMetadata>();
    
    private Set<CollectionMetadata> collectionMetadata = new HashSet<CollectionMetadata>();

    private ListableBeanFactory beanFactory;
    
    protected boolean hibernateConfigured = false;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.hibernateConfigured) {
            if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getBeanFactory(), SessionFactory.class).length > 0) {
                for (SessionFactory sessionFactory : BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, SessionFactory.class).values()) {
                    extractHibernateMetadata(sessionFactory);
                }
            }
        }
        super.afterPropertiesSet();
    }
    
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ListableBeanFactory.class, beanFactory, "HibernateConfigProcessor must be used in a ListableBeanFactory in order to auto-detect the necessary Hibernate configuration.");
        this.beanFactory = (ListableBeanFactory) beanFactory;        
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        extractHibernateMetadata(sessionFactory);
    }

    @Override
    protected Set<Class<?>> findTypesToRegister() {
        Set<Class<?>> typesToRegister = new HashSet<Class<?>>();
        if (hibernateConfigured) {
            for(ClassMetadata classMetadata : this.classMetadata) {
                typesToRegister.add(classMetadata.getMappedClass(EntityMode.POJO));
                findComponentProperties(classMetadata.getPropertyTypes(), typesToRegister);
            }
            for (CollectionMetadata collectionMetadata : this.collectionMetadata) {
                Type elementType = collectionMetadata.getElementType();
                if (elementType instanceof ComponentType) {
                    typesToRegister.add(elementType.getReturnedClass());
                    findComponentProperties(((ComponentType)elementType).getSubtypes(), typesToRegister);
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Hibernate types detected for AMF serialization support: "+typesToRegister.toString());
            }
        }
        return typesToRegister;
    }
    
    private void findComponentProperties(Type[] propertyTypes, Set<Class<?>> typesToRegister) {
        if (propertyTypes == null) {
            return;
        }
        for (Type propertyType : propertyTypes) {
            if (propertyType instanceof ComponentType) {
                typesToRegister.add(propertyType.getReturnedClass());
                findComponentProperties(((ComponentType)propertyType).getSubtypes(), typesToRegister);
            }
        }
    }

    @Override
    protected void configureConverters(ConverterRegistry registry) {
        registry.addConverter(new HibernateProxyConverter());
        registry.addConverterFactory(new PersistentCollectionConverterFactory());
    }
   
    protected ListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
    
    @SuppressWarnings("unchecked")
    protected void extractHibernateMetadata(SessionFactory sessionFactory) {
        this.classMetadata.addAll(sessionFactory.getAllClassMetadata().values());
        this.collectionMetadata.addAll(sessionFactory.getAllCollectionMetadata().values());
        this.hibernateConfigured = true;
    }
}
