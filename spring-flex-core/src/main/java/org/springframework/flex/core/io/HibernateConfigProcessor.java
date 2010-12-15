
package org.springframework.flex.core.io;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;

import flex.messaging.MessageBroker;
import flex.messaging.io.PropertyProxyRegistry;

public class HibernateConfigProcessor implements MessageBrokerConfigProcessor, BeanFactoryAware, InitializingBean {

    private Log log = LogFactory.getLog(HibernateConfigProcessor.class);
    
    private SessionFactory sessionFactory;

    private ConversionService conversionService;

    private ListableBeanFactory beanFactory;
    
    private boolean hibernateConfigured = false;

    public void afterPropertiesSet() throws Exception {
        if (this.sessionFactory == null) {
            if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getBeanFactory(), SessionFactory.class).length > 0) {
                this.sessionFactory = BeanFactoryUtils.beanOfTypeIncludingAncestors(beanFactory, SessionFactory.class);
                this.hibernateConfigured = true;
            }
        } else {
            this.hibernateConfigured = true;
        }
        this.conversionService = conversionService != null ? conversionService : getDefaultConversionService();
    }

    @SuppressWarnings("unchecked")
    public MessageBroker processAfterStartup(MessageBroker broker) {
        if (hibernateConfigured) {
            Iterator<ClassMetadata> it = this.sessionFactory.getAllClassMetadata().values().iterator();
            while (it.hasNext()) {
                SpringPropertyProxy proxy = new SpringPropertyProxy(it.next().getMappedClass(EntityMode.POJO), false);
                proxy.setConversionService(this.conversionService);
                PropertyProxyRegistry.getRegistry().register(proxy.getBeanType(), proxy);
            }
            log.info("Hibernate detected and AMF serialization support automatically installed successfully.");
        }
        return broker;
    }

    public MessageBroker processBeforeStartup(MessageBroker broker) {
        return broker;
    }
    
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ListableBeanFactory.class, beanFactory, "HibernateConfigProcessor must be used in a ListableBeanFactory in order to auto-detect the necessary Hibernate configuration.");
        this.beanFactory = (ListableBeanFactory) beanFactory;        
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }
   
    protected ListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    protected ConversionService getDefaultConversionService() {
        GenericConversionService conversionService = new GenericConversionService();
        conversionService.addConverter(new HibernateProxyConverter());
        conversionService.addConverterFactory(new PersistentCollectionConverterFactory());
        conversionService.addConverter(new NumberConverter());
        return conversionService;
    }
}
