package org.springframework.flex.core.io;

import java.lang.reflect.Constructor;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;


public class SpringPropertyProxyFactory {

    public static SpringPropertyProxy proxyFor(Class<?> beanType, boolean useDirectFieldAccess, ConversionService conversionService) {
        if(hasAmfCreator(beanType)) {
            SpringPropertyProxy proxy = new DelayedWriteSpringPropertyProxy(beanType, useDirectFieldAccess);
            proxy.setConversionService(conversionService);
            return proxy;
        } else {
            Assert.isTrue(ClassUtils.hasConstructor(beanType), "Failed to create SpringPropertyProxy for "+beanType.getName()+" - Classes mapped " +
            		"for deserialization from AMF must have either a no-arg default constructor, " +
            		"or a constructor annotated with "+AmfCreator.class.getName());
            SpringPropertyProxy proxy = new SpringPropertyProxy(beanType, useDirectFieldAccess);
            proxy.setConversionService(conversionService);
            return proxy;
        }
    }

    private static boolean hasAmfCreator(Class<?> beanType) {
        for (Constructor<?> constructor : beanType.getConstructors()) {
            if (constructor.getAnnotation(AmfCreator.class) != null) {
                return true;
            }
        }
        return false;
    }
}
