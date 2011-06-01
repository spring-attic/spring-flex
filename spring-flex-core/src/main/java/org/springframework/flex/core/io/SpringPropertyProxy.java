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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import flex.messaging.io.BeanProxy;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.amf.ASObject;

/**
 * Spring {@link ConversionService}-aware {@link PropertyProxy} that seeks to find an appropriate converter for 
 * a given bean property during AMF serialization and deserialization.
 * 
 * <p>
 * Uses Spring's {@link PropertyAccessor} interface for all property access, allowing for optional direct field access
 * on the objects being serialized/deserialized.
 *
 * @author Jeremy Grelle
 */
public class SpringPropertyProxy extends BeanProxy {

    private static final Log log = LogFactory.getLog(SpringPropertyProxy.class);
    
    private static final long serialVersionUID = 5374027421774405789L;
    
    private List<String> propertyNames;
    
    protected final ConversionService conversionService;
    
    protected final Class<?> beanType;
    
    protected final boolean useDirectFieldAccess;
    
    /**
     * Factory method for creating correctly configured Spring property proxy instances.
     * @param beanType the type being introspected
     * @param useDirectFieldAccess whether to access fields directly
     * @param conversionService the conversion service to use for property type conversion
     * @return a properly configured property proxy
     */
    public static SpringPropertyProxy proxyFor(Class<?> beanType, boolean useDirectFieldAccess, ConversionService conversionService) {
        if(PropertyProxyUtils.hasAmfCreator(beanType)) {
            SpringPropertyProxy proxy = new DelayedWriteSpringPropertyProxy(beanType, useDirectFieldAccess, conversionService);
            return proxy;
        } else {
            Assert.isTrue(ClassUtils.hasConstructor(beanType), "Failed to create SpringPropertyProxy for "+beanType.getName()+" - Classes mapped " +
                    "for deserialization from AMF must have either a no-arg default constructor, " +
                    "or a constructor annotated with "+AmfCreator.class.getName());
            SpringPropertyProxy proxy = new SpringPropertyProxy(beanType, useDirectFieldAccess, conversionService);
            
            try {
                //If possible, create an instance to introspect and cache the property names               
                Object instance = BeanUtils.instantiate(beanType);
                proxy.setPropertyNames(PropertyProxyUtils.findPropertyNames(conversionService, useDirectFieldAccess, instance));
            } catch(BeanInstantiationException ex) {
                //Property names can't be cached, but this is ok
            }
            
            return proxy;
        }
    }
    
    private SpringPropertyProxy(Class<?> beanType, boolean useDirectFieldAccess, ConversionService conversionService){
        super(null);
        this.beanType = beanType;
        this.useDirectFieldAccess = useDirectFieldAccess;
        this.conversionService = conversionService;
    }
    
    /**
     * The type for which this {@link PropertyProxy} is registered.
     * @return the bean type
     */
    public Class<?> getBeanType() {
        return beanType;
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the configured {@link ConversionService} to potentially convert the instance to the registered bean type.
     */
    @Override
    public Object getInstanceToSerialize(Object instance) {
        if (this.conversionService.canConvert(instance.getClass(), this.beanType)) {
            return this.conversionService.convert(instance, this.beanType);
        } else {
            return instance;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPropertyNames(Object instance) {
        if (!CollectionUtils.isEmpty(propertyNames) && instance.getClass().equals(this.beanType)) { 
            return this.propertyNames;
        } else {
            return PropertyProxyUtils.findPropertyNames(this.conversionService, this.useDirectFieldAccess, instance);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getType(Object instance, String propertyName) {
        return PropertyProxyUtils.getPropertyAccessor(this.conversionService, this.useDirectFieldAccess, instance).getPropertyType(propertyName);
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the configured {@link ConversionService} to potentially convert the current value to the actual type of the property.
     */
    @Override
    public Object getValue(Object instance, String propertyName) {
        PropertyAccessor accessor = PropertyProxyUtils.getPropertyAccessor(this.conversionService, this.useDirectFieldAccess, instance);
        Object value = accessor.getPropertyValue(propertyName);
        if(log.isDebugEnabled()) {
            getType(instance, propertyName);
            log.debug("Actual type of value for property '"+propertyName+"' on instance "+instance+" is "+(value != null ? value.getClass() : null));
        }
        
        TypeDescriptor targetType = accessor.getPropertyTypeDescriptor(propertyName);
        TypeDescriptor sourceType = value == null ? targetType : TypeDescriptor.valueOf(value.getClass());
        if (this.conversionService.canConvert(sourceType, targetType)) {
            value = this.conversionService.convert(value, sourceType, targetType);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteOnly(Object instance, String propertyName) {
        PropertyAccessor accessor = PropertyProxyUtils.getPropertyAccessor(this.conversionService, this.useDirectFieldAccess, instance);
        return isReadIgnored(instance, propertyName) || (!accessor.isReadableProperty(propertyName) && accessor.isWritableProperty(propertyName));
    }
    
    /**
     * {@inheritDoc}
     * 
     *  Delegates to the configured {@link ConversionService} to potentially convert the value to the actual type of the property.
     */
    @Override
    public void setValue(Object instance, String propertyName, Object value) {
        if (!isWriteIgnored(instance, propertyName)) {
            PropertyProxyUtils.getPropertyAccessor(this.conversionService, this.useDirectFieldAccess, instance).setPropertyValue(propertyName, value);
        }
    }

    private void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    private boolean isReadIgnored(Object instance, String propertyName) {
        PropertyAccessor accessor = PropertyProxyUtils.getPropertyAccessor(this.conversionService, this.useDirectFieldAccess, instance);
        if (!accessor.isReadableProperty(propertyName)) {
            return true;
        }
        if (this.useDirectFieldAccess) {
            AmfIgnoreField ignoreField = (AmfIgnoreField) accessor.getPropertyTypeDescriptor(propertyName).getAnnotation(AmfIgnoreField.class);
            return ignoreField != null && ignoreField.onSerialization();
        } else {
            PropertyDescriptor pd = ((BeanWrapper)accessor).getPropertyDescriptor(propertyName);
            return pd.getReadMethod().getAnnotation(AmfIgnore.class) != null;
        }
    }
    
    private boolean isWriteIgnored(Object instance, String propertyName) {
        PropertyAccessor accessor = PropertyProxyUtils.getPropertyAccessor(this.conversionService, this.useDirectFieldAccess, instance);
        if (!accessor.isWritableProperty(propertyName)) {
            return true;
        }
        if (this.useDirectFieldAccess) {
            AmfIgnoreField ignoreField = (AmfIgnoreField) accessor.getPropertyTypeDescriptor(propertyName).getAnnotation(AmfIgnoreField.class);
            return ignoreField != null && ignoreField.onDeserialization();
        } else {
            PropertyDescriptor pd = ((BeanWrapper)accessor).getPropertyDescriptor(propertyName);
            return pd.getWriteMethod().getAnnotation(AmfIgnore.class) != null;
        }
    }
    
    /**
     * Extension to {@link SpringPropertyProxy} that allow for use of classes that lack default no-arg constructors and instead have
     * a constructor annotated with {@link AmfCreator}.
     *
     * @author Jeremy Grelle
     */
    static final class DelayedWriteSpringPropertyProxy extends SpringPropertyProxy {

        private static final long serialVersionUID = -5330475591068260312L;
        
        private final Constructor<?> amfConstructor;
        private final List<String> paramNames = new ArrayList<String>();
        
        private DelayedWriteSpringPropertyProxy(Class<?> beanType, boolean useDirectFieldAccess, ConversionService conversionService) {
            super(beanType, useDirectFieldAccess, conversionService);
            this.amfConstructor = findAmfConstructor();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object createInstance(String className) {
            Assert.isTrue(this.beanType.getName().equals(className), "Asked to create instance of an unknown type.");
            return new ASObject(className);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object instanceComplete(Object instance) {
            Assert.isInstanceOf(ASObject.class, instance, "Expected an instance of "+ASObject.class.getName());
            ASObject sourceInstance = (ASObject) instance;
            Assert.notNull(sourceInstance.getType(), "Expected an explicit type to be set on the ASObject instance passed to this PropertyProxy.");
            Object targetInstance = createTargetInstance(sourceInstance);
            applyPropertyValues(sourceInstance, targetInstance);
            return targetInstance;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void setValue(Object instance, String propertyName, Object value) {
            if (instance instanceof ASObject) {
                ((ASObject) instance).put(propertyName, value);
            } else {
                super.setValue(instance, propertyName, value);
            }
        }

        private Object createTargetInstance(ASObject sourceInstance) {
            Object[] params = new Object[this.paramNames.size()];
            for (int i=0; i<params.length; i++) {
                Object value = sourceInstance.remove(this.paramNames.get(i));
                TypeDescriptor targetType = TypeDescriptor.valueOf(this.amfConstructor.getParameterTypes()[i]);
                TypeDescriptor sourceType = value == null ? targetType : TypeDescriptor.valueOf(value.getClass());
                if (this.conversionService.canConvert(sourceType, targetType)) {
                    value = this.conversionService.convert(value, sourceType, targetType);
                }
                params[i] = value;
            }
            try {
                return this.amfConstructor.newInstance(params);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to invoke constructor marked with "+AmfCreator.class.getName()+" for type "+this.beanType, ex);
            }
        }
        
        private Object applyPropertyValues(ASObject sourceInstance, Object targetInstance) {
            for (Object property : sourceInstance.keySet()) {
                setValue(targetInstance, property.toString(), sourceInstance.get(property));
            }
            return targetInstance;
        }
        
        private Constructor<?> findAmfConstructor() {
            for (Constructor<?> c : this.beanType.getConstructors()) {
                if (c.isAnnotationPresent(AmfCreator.class)) {
                    Assert.isTrue(c.getParameterAnnotations().length == c.getParameterTypes().length, "Found a constructor marked with "+AmfCreator.class.getName()+" but not all of its parameters are marked with "+AmfProperty.class.getName());
                    for (Annotation[] paramAnnotations : c.getParameterAnnotations()) {
                        boolean hasAmfProperty = false;
                        for (Annotation paramAnnotation : paramAnnotations) {
                            if (paramAnnotation.annotationType().equals(AmfProperty.class)) {
                                hasAmfProperty = true;
                                this.paramNames.add(((AmfProperty)paramAnnotation).value());
                                break;
                            }
                        }
                        Assert.isTrue(hasAmfProperty, "Found a constructor marked with "+AmfCreator.class.getName()+" but not all of its parameters are marked with "+AmfProperty.class.getName());
                    }
                    return c;
                }
            }
            throw new IllegalStateException("An instance of "+this.beanType+" could note be created.  Must either have a public no-arg constructor, or a constructor annotated with "+AmfCreator.class.getName()+".");
        }
    }
}
