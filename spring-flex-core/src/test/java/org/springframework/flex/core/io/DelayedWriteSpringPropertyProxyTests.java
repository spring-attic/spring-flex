package org.springframework.flex.core.io;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.domain.ImmutableValueObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;


public class DelayedWriteSpringPropertyProxyTests {

	AmfMessageSerializer   serializer;
	AmfMessageDeserializer deserializer;

	AmfTrace serializerTrace;
	AmfTrace deserializerTrace;

	MockHttpServletResponse response;
	MockHttpServletRequest  request;

	@Before
	public void setUp() {
		GenericConversionService cs = new GenericConversionService();
		cs.addConverter(new NumberConverter());
		SpringPropertyProxy voProxy = SpringPropertyProxy.proxyFor(ImmutableValueObject.class, false, cs);
		PropertyProxyRegistry.getRegistry().register(ImmutableValueObject.class, voProxy);

		this.serializer = new AmfMessageSerializer();
		this.serializerTrace = new AmfTrace();
		this.response = new MockHttpServletResponse();
		this.serializer.initialize(new SerializationContext(), response.getOutputStream(), serializerTrace);
		this.deserializer = new AmfMessageDeserializer();
		this.deserializerTrace = new AmfTrace();
		this.request = new MockHttpServletRequest();
	}

	@Test
	public void deserializeAnnotatedImmutableObject() throws IOException, ClassNotFoundException {
		ImmutableValueObject data = new ImmutableValueObject("bar", new Integer(1));
		data.setVoRef(new ImmutableValueObject("zed", new Integer(5)));
		serialize(data);

		ImmutableValueObject result = (ImmutableValueObject) deserialize();

		Assert.assertEquals("bar", result.getFoo());
		Assert.assertEquals(new Integer(1), result.getZoo());
		Assert.assertNotNull(result.getVoRef());
		Assert.assertEquals("zed", result.getVoRef().getFoo());
		Assert.assertEquals(new Integer(5), result.getVoRef().getZoo());
	}

	private void serialize(Object data) throws IOException {
		MessageBody body = new MessageBody();
		body.setData(data);
		serializer.writeBody(body);
	}

	private Object deserialize() throws ClassNotFoundException, IOException {
		this.request.setContent(this.response.getContentAsByteArray());
		this.deserializer.initialize(new SerializationContext(), this.request.getInputStream(), deserializerTrace);
		MessageBody body = new MessageBody();
		this.deserializer.readBody(body, 0);
		return body.getData();
	}
}
