/*
 * Copyright 2002-2010 the original author or authors.
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import flex.messaging.io.BeanProxy;

/**
 * Spring {@link ConversionService}-aware {@link BeanProxy} that seeks to find an appropriate converter for 
 * a given bean property during serialization and deserialization.
 *
 * @author Jeremy Grelle
 */
public class SpringPropertyProxy extends BeanProxy {

    private static final Log log = LogFactory.getLog(SpringPropertyProxy.class);
    
    private static final long serialVersionUID = 5374027421774405789L;
    
    private ConversionService conversionService = new GenericConversionService();
    
    private Class<?> beanType;
    
    private final boolean useDirectFieldAccess;
    
    //TODO - Cache metadata for a given type
    
    public SpringPropertyProxy(Class<?> beanType, boolean useDirectFieldAccess) {
        super(null);
        this.beanType = beanType;
        this.useDirectFieldAccess = useDirectFieldAccess;
    }
    
    @Override
    public List<String> getPropertyNames(Object instance) {
        //TODO - Consider an option hear to leave out uninitialized properties altogether instead of just returning null
        if (this.useDirectFieldAccess) {
            return getFieldNames(instance);
        } else {
            return getBeanPropertyNames(instance);
        }
    }

    @Override
    public Class<?> getType(Object instance, String propertyName) {
        return getPropertyAccessor(instance).getPropertyType(propertyName);
    }

    @Override
    public Object getValue(Object instance, String propertyName) {
        PropertyAccessor accessor = getPropertyAccessor(instance);
        Object value = accessor.getPropertyValue(propertyName);
        if(log.isDebugEnabled()) {
            getType(instance, propertyName);
            log.debug("Actual type of value for property '"+propertyName+"' on instance "+instance+" is "+(value != null ? value.getClass() : null));
        }
        if(value == null) {
            return null;
        }
        TypeDescriptor targetType = accessor.getPropertyTypeDescriptor(propertyName);
        TypeDescriptor sourceType = TypeDescriptor.valueOf(value.getClass());
        if (!sourceType.getType().equals(targetType.getType()) && this.conversionService.canConvert(sourceType, targetType)) {
            value = this.conversionService.convert(value, sourceType, targetType);
        }
        return value;
    }

    @Override
    public void setValue(Object instance, String propertyName, Object value) {
        getPropertyAccessor(instance).setPropertyValue(propertyName, value);
    }
    
    public ConversionService getConversionService() {
        return this.conversionService;
    }
    
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    private List<String> getBeanPropertyNames(Object instance) {
        List<String> names = new ArrayList<String>(); 
        for (PropertyDescriptor pd : ((BeanWrapper)getPropertyAccessor(instance)).getPropertyDescriptors()) {
            if (!pd.getName().equals("class")) {
                names.add(pd.getName());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Property names for "+instance+" : "+names);
        }
        return names;
    }
    
    private List<String> getFieldNames(Object instance) {
        final List<String> names = new ArrayList<String>();
        ReflectionUtils.doWithFields(instance.getClass(), new FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                names.add(field.getName());
            }
        });
        return names;
    }
    
    private PropertyAccessor getPropertyAccessor(Object instance) {
        ConfigurablePropertyAccessor accessor = null;
        if (this.useDirectFieldAccess) {
            accessor = PropertyAccessorFactory.forDirectFieldAccess(instance);
        } else {
            accessor = PropertyAccessorFactory.forBeanPropertyAccess(instance);
        }
        accessor.setConversionService(this.conversionService);
        return accessor;
    }

    public Object getInstanceToSerialize(Object instance) {
        if (this.conversionService.canConvert(instance.getClass(), this.beanType)) {
            return this.conversionService.convert(instance, this.beanType);
        } else {
            return instance;
        }
    }
    
    public Class<?> getBeanType() {
        return beanType;
    }
}
