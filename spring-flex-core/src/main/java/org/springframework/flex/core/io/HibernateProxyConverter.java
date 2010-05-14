package org.springframework.flex.core.io;

import java.util.Collections;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.PropertyTypeDescriptor;
import org.springframework.util.Assert;


public class HibernateProxyConverter implements GenericConverter {
    
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.isInstanceOf(HibernateProxy.class, source, "Expected an instance of HibernateProxy to convert");
        Assert.isAssignable(HibernateProxy.class, sourceType.getType(), "Expected a subclass of HibernateProxy for the source type");
        HibernateProxy proxy = (HibernateProxy) source;
        if (targetType instanceof PropertyTypeDescriptor) {
            //Could do an additional check here for specific Hibernate/JPA annotations, 
            //but just taking this action no matter what if it's a property is good enough for now
            if (!Hibernate.isInitialized(proxy)) {
                return null;
            }
        }
        return proxy.getHibernateLazyInitializer().getImplementation();
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(HibernateProxy.class, Object.class));
    }

}
