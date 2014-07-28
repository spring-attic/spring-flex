package org.springframework.flex.hibernate4.core.io;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.hibernate4.core.io.domain.ImmutableValueObject;
import org.springframework.flex.hibernate4.core.io.domain.MaritalStatus;
import org.springframework.flex.hibernate4.core.io.domain.Person;

import flex.messaging.io.PropertyProxy;


public class SpringPropertyProxyCreationTests {

    @Test
    public void defaultConstructorObject() {
        PropertyProxy result = SpringPropertyProxy.proxyFor(Person.class, false, new GenericConversionService());
        Assert.assertEquals(SpringPropertyProxy.class, result.getClass());
    }

    @Test
    public void testEnum() {
        PropertyProxy result = SpringPropertyProxy.proxyFor(MaritalStatus.class, false, new GenericConversionService());
        Assert.assertEquals(SpringPropertyProxy.class, result.getClass());
    }

    @Test
    public void amfCreatorAnnotatedImmutableObject() {
        PropertyProxy result = SpringPropertyProxy.proxyFor(ImmutableValueObject.class, false, new GenericConversionService());
        Assert.assertEquals(SpringPropertyProxy.DelayedWriteSpringPropertyProxy.class, result.getClass());
    }

}
