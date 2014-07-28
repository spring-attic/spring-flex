package org.springframework.flex.core.io;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.domain.ImmutableValueObject;
import org.springframework.flex.core.io.domain.MaritalStatus;

import flex.messaging.io.PropertyProxy;


public class SpringPropertyProxyCreationTests {

	@Test
	public void testEnum() {
		PropertyProxy result = SpringPropertyProxy.proxyFor(MaritalStatus.class, false, new GenericConversionService());
		assertEquals(SpringPropertyProxy.class, result.getClass());
	}

	@Test
	public void amfCreatorAnnotatedImmutableObject() {
		PropertyProxy result = SpringPropertyProxy.proxyFor(ImmutableValueObject.class, false, new GenericConversionService());
		assertEquals(SpringPropertyProxy.DelayedWriteSpringPropertyProxy.class, result.getClass());
	}

}
