
package org.springframework.flex.core.io;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.persistence.EntityManagerFactory;

import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.io.PropertyProxyRegistry;

public class HibernateConfigProcessor implements MessageBrokerConfigProcessor {

    private SessionFactory sessionFactory;

    private ConversionService conversionService;

    public HibernateConfigProcessor(EntityManagerFactory entityManagerFactory) {
        this(entityManagerFactory, null);
    }

    public HibernateConfigProcessor(EntityManagerFactory entityManagerFactory, ConversionService conversionService) {
        Method gsfMethod = ReflectionUtils.findMethod(entityManagerFactory.getClass(), "getSessionFactory");
        Assert.notNull(gsfMethod, "Could not retrieve the underlying Hibernate SessionFactory from the provided EntityManagerFactory");
        SessionFactory sessionFactory = (SessionFactory) ReflectionUtils.invokeMethod(gsfMethod, entityManagerFactory);
        init(sessionFactory, conversionService);
    }

    public HibernateConfigProcessor(SessionFactory sessionFactory) {
        this(sessionFactory, null);
    }

    public HibernateConfigProcessor(SessionFactory sessionFactory, ConversionService conversionService) {
        init(sessionFactory, conversionService);
    }

    private void init(SessionFactory sessionFactory, ConversionService conversionService) {
        Assert.notNull(sessionFactory, "The Hibernate SessionFactory must not be null.");
        this.sessionFactory = sessionFactory;
        this.conversionService = conversionService != null ? conversionService : getDefaultConversionService();
    }

    @SuppressWarnings("unchecked")
    public MessageBroker processAfterStartup(MessageBroker broker) {
        Iterator<ClassMetadata> it = this.sessionFactory.getAllClassMetadata().values().iterator();
        while (it.hasNext()) {
            SpringPropertyProxy proxy = new SpringPropertyProxy(it.next().getMappedClass(EntityMode.POJO));
            proxy.setConversionService(this.conversionService);
            PropertyProxyRegistry.getRegistry().register(proxy.getBeanType(), proxy);
        }
        return broker;
    }

    public MessageBroker processBeforeStartup(MessageBroker broker) {
        return broker;
    }

    private ConversionService getDefaultConversionService() {
        GenericConversionService conversionService = new GenericConversionService();
        conversionService.addConverter(new HibernateProxyConverter());
        conversionService.addConverterFactory(new PersistentCollectionConverterFactory());
        return conversionService;
    }

}
