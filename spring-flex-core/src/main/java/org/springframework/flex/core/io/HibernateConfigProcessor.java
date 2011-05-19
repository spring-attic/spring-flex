
package org.springframework.flex.core.io;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.util.Assert;


public class HibernateConfigProcessor extends ConversionServiceConfigProcessor implements BeanFactoryAware, InitializingBean {

    private Log log = LogFactory.getLog(HibernateConfigProcessor.class);
    
    private SessionFactory sessionFactory;

    private ListableBeanFactory beanFactory;
    
    private boolean hibernateConfigured = false;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.sessionFactory == null) {
            if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getBeanFactory(), SessionFactory.class).length > 0) {
                this.sessionFactory = BeanFactoryUtils.beanOfTypeIncludingAncestors(beanFactory, SessionFactory.class);
                this.hibernateConfigured = true;
            }
        } else {
            this.hibernateConfigured = true;
        }
        super.afterPropertiesSet();
    }
    
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ListableBeanFactory.class, beanFactory, "HibernateConfigProcessor must be used in a ListableBeanFactory in order to auto-detect the necessary Hibernate configuration.");
        this.beanFactory = (ListableBeanFactory) beanFactory;        
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<Class<?>> findTypesToRegister() {
        Set<Class<?>> types = new HashSet<Class<?>>();
        if (hibernateConfigured) {
            Iterator<ClassMetadata> classIt = this.sessionFactory.getAllClassMetadata().values().iterator();
            while (classIt.hasNext()) {
                ClassMetadata classMetadata = classIt.next();
                types.add(classMetadata.getMappedClass(EntityMode.POJO));
                for (Type propertyType : classMetadata.getPropertyTypes()) {
                    if (propertyType.isComponentType()) {
                        types.add(propertyType.getReturnedClass());
                    }
                }
            }
            Iterator<CollectionMetadata> collIt = this.sessionFactory.getAllCollectionMetadata().values().iterator();
            while (collIt.hasNext()) {
                Type elementType = collIt.next().getElementType();
                if (elementType.isComponentType()) {
                    types.add(elementType.getReturnedClass());
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Hibernate types detected and for AMF serialization support: "+types.toString());
            }
        }
        return types;
    }

    @Override
    protected void configureConverters(ConverterRegistry registry) {
        registry.addConverter(new HibernateProxyConverter());
        registry.addConverterFactory(new PersistentCollectionConverterFactory());
    }

	protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }
   
    protected ListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
