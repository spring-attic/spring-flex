package org.springframework.flex.core.io;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

import flex.messaging.io.amf.ASObject;


public class DelayedWriteSpringPropertyProxy extends SpringPropertyProxy {

    private static final long serialVersionUID = -5330475591068260312L;
    
    private final Constructor<?> amfConstructor;
    private final List<String> paramNames = new ArrayList<String>();
    
    public DelayedWriteSpringPropertyProxy(Class<?> beanType, boolean useDirectFieldAccess) {
        super(beanType, useDirectFieldAccess);
        this.amfConstructor = findAmfConstructor();
    }

    @Override
    public Object createInstance(String className) {
        Assert.isTrue(getBeanType().getName().equals(className), "Asked to create instance of an unknown type.");
        return new ASObject(className);
    }

    @Override
    public Object instanceComplete(Object instance) {
        Assert.isInstanceOf(ASObject.class, instance, "Expected an instance of "+ASObject.class.getName());
        ASObject sourceInstance = (ASObject) instance;
        Assert.notNull(sourceInstance.getType(), "Expected an explicit type to be set on the ASObject instance passed to this PropertyProxy.");
        Object targetInstance = createTargetInstance(sourceInstance);
        applyPropertyValues(sourceInstance, targetInstance);
        return targetInstance;
    }

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
            TypeDescriptor sourceType = TypeDescriptor.valueOf(value.getClass());
            TypeDescriptor targetType = TypeDescriptor.valueOf(this.amfConstructor.getParameterTypes()[i]);
            if (!sourceType.getType().equals(targetType.getType()) && getConversionService().canConvert(sourceType, targetType)) {
                value = getConversionService().convert(value, sourceType, targetType);
            }
            params[i] = value;
        }
        try {
            return this.amfConstructor.newInstance(params);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to invoke constructor marked with "+AmfCreator.class.getName()+" for type "+this.getBeanType(), ex);
        }
    }
    
    private Object applyPropertyValues(ASObject sourceInstance, Object targetInstance) {
        for (Object property : sourceInstance.keySet()) {
            setValue(targetInstance, property.toString(), sourceInstance.get(property));
        }
        return targetInstance;
    }
    
    private Constructor<?> findAmfConstructor() {
        for (Constructor<?> c : getBeanType().getConstructors()) {
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
        throw new IllegalStateException("An instance of "+getBeanType()+" could note be created.  Must either have a public no-arg constructor, or a constructor annotated with "+AmfCreator.class.getName()+".");
    }
}
