package org.springframework.flex.core.io;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.Assert;

import flex.messaging.io.BeanProxy;


public class SpringPropertyProxy extends BeanProxy {

    private static final Log log = LogFactory.getLog(SpringPropertyProxy.class);
    
    private static final long serialVersionUID = 5374027421774405789L;
    
    private ConversionService conversionService = new GenericConversionService();
    
    private Class<?> beanType;

    public SpringPropertyProxy(Class<?> beanType) {
        super(null);
        this.beanType = beanType;
    }
    
    public SpringPropertyProxy(Object defaultInstance) {
        super(defaultInstance);
        Assert.notNull(defaultInstance, "Default instance cannot be null");
        this.beanType = defaultInstance.getClass();
    }

    @Override
    public List<String> getPropertyNames(Object instance) {
        //TODO - Support direct field access
        //TODO - Consider an option hear to leave out uninitialized properties altogether instead of just returning null
        return getBeanPropertyNames(instance);
    }

    @Override
    public Class<?> getType(Object instance, String propertyName) {
        Class<?> type = getBeanPropertyAccessor(instance).getPropertyType(propertyName);
        if(log.isDebugEnabled()){
            log.debug("Introspected type of property '"+propertyName+"' on instance "+instance+" is "+type);
        }
        return type;
    }

    @Override
    public Object getValue(Object instance, String propertyName) {
        BeanWrapper wrapper = getBeanPropertyAccessor(instance);
        Object value = getBeanPropertyAccessor(instance).getPropertyValue(propertyName);
        if(log.isDebugEnabled()) {
            getType(instance, propertyName);
            log.debug("Actual type of value for property '"+propertyName+"' on instance "+instance+" is "+(value != null ? value.getClass() : null));
        }
        if(value == null) {
            return null;
        }
        TypeDescriptor targetType = wrapper.getPropertyTypeDescriptor(propertyName);
        TypeDescriptor sourceType = TypeDescriptor.valueOf(value.getClass());
        if (!sourceType.equals(targetType) && this.conversionService.canConvert(sourceType, targetType)) {
            value = this.conversionService.convert(value, sourceType, targetType);
        }
        return value;
    }

    @Override
    public void setValue(Object instance, String propertyName, Object value) {
        getBeanPropertyAccessor(instance).setPropertyValue(propertyName, value);
    }
    
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    private List<String> getBeanPropertyNames(Object instance) {
        List<String> names = new ArrayList<String>(); 
        for (PropertyDescriptor pd : getBeanPropertyAccessor(instance).getPropertyDescriptors()) {
            if (!pd.getName().equals("class")) {
                names.add(pd.getName());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Property names for "+instance+" : "+names);
        }
        return names;
    }
    
    private BeanWrapper getBeanPropertyAccessor(Object instance) {
        BeanWrapper accessor = PropertyAccessorFactory.forBeanPropertyAccess(instance);
        accessor.setConversionService(this.conversionService);
        return accessor;
    }
    
    /*private PropertyAccessor getFieldAccessor(Object instance) {
        ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(instance);
        accessor.setConversionService(this.conversionService);
        return accessor;
    }*/

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
