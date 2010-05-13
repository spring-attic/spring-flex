package org.springframework.flex.core.io;

import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;


public class DefaultPersistentCollectionConverterFactory implements ConverterFactory<PersistentCollection, Object> {

    public <T> Converter<PersistentCollection, T> getConverter(Class<T> targetType) {
        return new Converter<PersistentCollection, T>() {
            @SuppressWarnings("unchecked")
            public T convert(PersistentCollection source) {
                if (!Hibernate.isInitialized(source)) {
                    return null;
                } else {
                    return (T) source;
                }
            }  
        };
    }

}
