package org.springframework.flex.core.io;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;

import flex.messaging.io.BeanProxy;


public class SpringPropertyProxy extends BeanProxy {

    private static final long serialVersionUID = 5374027421774405789L;
    
    private ConversionService conversionService;

    protected SpringPropertyProxy(Object defaultInstance) {
        super(defaultInstance);
    }

    public List getPropertyNames(Object instance) {
        //TODO - Support direct field access
        return getBeanPropertyNames(instance);
    }

    public Class getType(Object instance, String propertyName) {
        return getBeanPropertyAccessor(instance).getPropertyType(propertyName);
    }

    public Object getValue(Object instance, String propertyName) {
        return getBeanPropertyAccessor(instance).getPropertyValue(propertyName);
    }

    public void setValue(Object instance, String propertyName, Object value) {
        getBeanPropertyAccessor(instance).setPropertyValue(propertyName, value);
    }
    
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    private List<String> getBeanPropertyNames(Object instance) {
        List<String> names = new ArrayList<String>(); 
        for (PropertyDescriptor pd : getBeanPropertyAccessor(instance).getPropertyDescriptors()) {
             names.add(pd.getName());
        }
        return names;
    }
    
    private BeanWrapper getBeanPropertyAccessor(Object instance) {
        BeanWrapper accessor = PropertyAccessorFactory.forBeanPropertyAccess(instance);
        accessor.setConversionService(this.conversionService);
        return accessor;
    }
    
    private PropertyAccessor getFieldAccessor(Object instance) {
        ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(instance);
        accessor.setConversionService(this.conversionService);
        return accessor;
    }

    //This is likely a good place to plug in various strategies for handling Hibernate lazy initialization
    public Object getInstanceToSerialize(Object instance) {
        return instance;
    }
}
