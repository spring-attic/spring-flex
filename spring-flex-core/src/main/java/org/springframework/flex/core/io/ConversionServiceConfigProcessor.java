package org.springframework.flex.core.io;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.config.MessageBrokerConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.io.PropertyProxyRegistry;


public class ConversionServiceConfigProcessor implements MessageBrokerConfigProcessor, InitializingBean {

    private ConversionService conversionService;
    
    private boolean useDirectFieldAccess = false;
    
    public void afterPropertiesSet() throws Exception {
        this.conversionService = conversionService != null ? conversionService : getDefaultConversionService();
    }

    public MessageBroker processAfterStartup(MessageBroker broker) {
        registerAmfProxies(this.conversionService, this.useDirectFieldAccess);
        return broker;
    }

    public MessageBroker processBeforeStartup(MessageBroker broker) {
        return broker;
    }
    
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    protected void registerAmfProxies(ConversionService conversionService, boolean useDirectFieldAccess) {
        Set<Class<?>> typesToRegister = findTypesToRegister();
        for (Class<?> type : typesToRegister) {
            registerPropertyProxy(SpringPropertyProxyFactory.proxyFor(type, useDirectFieldAccess, conversionService));
        }
    }
    
    protected void registerPropertyProxy(SpringPropertyProxy proxy) {
        PropertyProxyRegistry.getRegistry().register(proxy.getBeanType(), proxy);
    }
    
    protected Set<Class<?>> findTypesToRegister() {
        return new HashSet<Class<?>>();
    }
    
    /**
     * Template method to allow subclasses to configure their own set of {@link Converter} instances.  This is a 
     * convenient alternative to supplying a completely custom-configured {@link ConversionService} instance.
     * 
     * @param registry - the converter registry used by the {@link ConversionService}
     */
    protected void configureConverters(ConverterRegistry registry) {
        //default no-op
    }
    
    private ConversionService getDefaultConversionService() {
        GenericConversionService conversionService = new GenericConversionService();
        configureConverters(conversionService);
        conversionService.addConverter(new NumberConverter());
        return conversionService;
    }

    public void setUseDirectFieldAccess(boolean useDirectFieldAccess) {
    	this.useDirectFieldAccess = useDirectFieldAccess;
    }
}
