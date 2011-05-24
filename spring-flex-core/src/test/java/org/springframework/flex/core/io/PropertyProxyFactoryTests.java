package org.springframework.flex.core.io;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.domain.ImmutableValueObject;
import org.springframework.flex.core.io.domain.Person;

import flex.messaging.io.PropertyProxy;


public class PropertyProxyFactoryTests {

    @Test
    public void testDefaultConstructorObject() {
        PropertyProxy result = SpringPropertyProxyFactory.proxyFor(Person.class, false, new GenericConversionService());
        assertEquals(SpringPropertyProxy.class, result.getClass());
    }
    
    @Test
    public void testAmfCreatorAnnotatedImmutableObject() {
        PropertyProxy result = SpringPropertyProxyFactory.proxyFor(ImmutableValueObject.class, false, new GenericConversionService());
        assertEquals(DelayedWriteSpringPropertyProxy.class, result.getClass());
    }

}
