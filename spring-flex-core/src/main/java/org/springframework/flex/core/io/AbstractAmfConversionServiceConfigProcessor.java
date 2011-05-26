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

import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.config.MessageBrokerConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.io.PropertyProxyRegistry;

/**
 * Base {@link MessageBrokerConfigProcessor} implementation that installs an alternative {@link PropertyProxy} implementation for 
 * enhanced serialization and deserialization of specific types.  The alternative implementation uses Sping's {@link ConversionService} 
 * to allow specialized conversion of certain property types and is more flexible in the types it can handle.
 * 
 * <p>
 * The installed {@code SpringPropertyProxy} instances may be configured to use field-based property access (instead of requiring getters and setters) by 
 * setting the {@link AbstractAmfConversionServiceConfigProcessor#setUseDirectFieldAccess(boolean) useDirectFieldAccess} property to {@code true}.  Additionally, 
 * types that do not have a default no-arg constructur can be handled, as long as they provide a constructor annotated with {@link AmfCreator}.
 * 
 * <p>
 * Subclasses are expected to supply their own mechanisms for determining the list of types to register and may apply additional configuration of the 
 * {@code ConversionService}, such as provisioning additional custom {@link Converter Converters}.  
 * 
 * @see SpringPropertyProxy
 *
 * @author Jeremy Grelle
 */
public abstract class AbstractAmfConversionServiceConfigProcessor implements MessageBrokerConfigProcessor, InitializingBean {

    private ConversionService conversionService;
    
    private boolean useDirectFieldAccess = false;
    
    /**
     * 
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        this.conversionService = conversionService != null ? conversionService : getDefaultConversionService();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public final MessageBroker processAfterStartup(MessageBroker broker) {
        registerAmfProxies(this.conversionService, this.useDirectFieldAccess);
        return broker;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public final MessageBroker processBeforeStartup(MessageBroker broker) {
        return broker;
    }
    
    /**
     * Sets the {@link ConversionService} implementation to be used by all registered {@link SpringPropertyProxy} instances.
     * @param conversionService the conversion service to be used
     */
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    /**
     * When true, configures the registered {@link SpringPropertyProxy} instances to access fields directly, rather than 
     * requiring strict JavaBean compliance.  Defaults to false.
     * 
     * @param useDirectFieldAccess determines whether fields should be accessed directly.
     */
    public void setUseDirectFieldAccess(boolean useDirectFieldAccess) {
        this.useDirectFieldAccess = useDirectFieldAccess;
    }
    
    /**
     * Called during initialization, the default implementation configures and registers a {@link SpringPropertyProxy} instance 
     * for each type returned by {@link AbstractAmfConversionServiceConfigProcessor#findTypesToRegister() findTypesToRegister}. 
     * @param conversionService the conversion service to be used for property conversion
     * @param useDirectFieldAccess determines whether fields should be accessed directly
     */
    protected void registerAmfProxies(ConversionService conversionService, boolean useDirectFieldAccess) {
        Set<Class<?>> typesToRegister = findTypesToRegister();
        for (Class<?> type : typesToRegister) {
            registerPropertyProxy(SpringPropertyProxy.proxyFor(type, useDirectFieldAccess, conversionService));
        }
    }
    
    /**
     * Registers the given {@link SpringPropertyProxy} with the BlazeDS {@link PropertyProxyRegistry}.
     * @param proxy the property proxy to register
     */
    protected void registerPropertyProxy(SpringPropertyProxy proxy) {
        PropertyProxyRegistry.getRegistry().register(proxy.getBeanType(), proxy);
    }
    
    /**
     * Returns the set of types for which {@link SpringPropertyProxy} instances should be registered.
     * @return the set of types to register
     */
    protected abstract Set<Class<?>> findTypesToRegister();
    
    /**
     * Template method to allow subclasses to configure their own set of {@link Converter} instances.  This is a 
     * convenient alternative to supplying a completely custom-configured {@link ConversionService} instance.  
     * 
     * <p>
     * The default implementation does not register any additional {@link Converters}, thus subclasses do not need to delegate to it.
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
}
