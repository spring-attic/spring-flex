package org.springframework.flex.core.io;

import java.util.Iterator;

import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.config.MessageBrokerConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.io.PropertyProxyRegistry;


public class HibernateConfigProcessor implements MessageBrokerConfigProcessor {

    private final SessionFactory sessionFactory;
    
    private final ConversionService conversionService;
    
    public HibernateConfigProcessor(SessionFactory sessionFactory) {
        this(sessionFactory, null);
    }
    
    public HibernateConfigProcessor(SessionFactory sessionFactory, ConversionService conversionService) {
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
