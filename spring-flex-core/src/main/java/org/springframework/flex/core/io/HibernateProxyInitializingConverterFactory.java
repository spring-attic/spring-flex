package org.springframework.flex.core.io;

import org.hibernate.proxy.HibernateProxy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;


public class HibernateProxyInitializingConverterFactory implements ConverterFactory<HibernateProxy, Object> {

    public <T> Converter<HibernateProxy, T> getConverter(Class<T> targetType) {
        return new Converter<HibernateProxy, T>() {
            @SuppressWarnings("unchecked")
            public T convert(HibernateProxy source) {
                return (T) source.getHibernateLazyInitializer().getImplementation();
            }
        };
    }
}
