package org.springframework.flex.core.io;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.flex.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Static helper util methods for Spring-based property access and introspection.
 *
 * @author Jeremy Grelle
 */
class PropertyProxyUtils {

    private static final Log log = LogFactory.getLog(PropertyProxyUtils.class);
    
    private PropertyProxyUtils(){}

    public static boolean hasAmfCreator(Class<?> beanType) {
        for (Constructor<?> constructor : beanType.getConstructors()) {
            if (constructor.getAnnotation(AmfCreator.class) != null) {
                return true;
            }
        }
        return false;
    }

    public static PropertyAccessor getPropertyAccessor(ConversionService conversionService, boolean useDirectFieldAccess, Object instance) {
        ConfigurablePropertyAccessor accessor = null;
        if (useDirectFieldAccess) {
	        accessor = PropertyAccessorFactory.forDirectFieldAccess(instance);
        } else {
            accessor = PropertyAccessorFactory.forBeanPropertyAccess(instance);
        }
        accessor.setConversionService(conversionService);
        return accessor;
    }

    public static List<String> getFieldNames(Object instance) {
        final List<String> names = new ArrayList<String>();
        ReflectionUtils.doWithFields(instance.getClass(), new FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                names.add(field.getName());
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("Property names for "+instance+" : "+names);
        }
        return names;
    }

    public static List<String> getBeanPropertyNames(BeanWrapper beanWrapper) {
        List<String> names = new ArrayList<String>(); 
        for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
            if (!pd.getName().equals("class")) {
                names.add(pd.getName());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Property names for "+beanWrapper.getWrappedInstance()+" : "+names);
        }
        return names;
    }

    public static List<String> findPropertyNames(ConversionService conversionService, boolean useDirectFieldAccess, Object instance) {
        if (useDirectFieldAccess) {
            return getFieldNames(instance);
        } else {
            return getBeanPropertyNames((BeanWrapper)getPropertyAccessor(conversionService, useDirectFieldAccess, instance));
        }
    }
}
