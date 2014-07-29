package org.springframework.flex.hibernate3.core.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.NumberConverter;
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.hibernate3.core.io.domain.IgnorablePropsObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.BeanProxy;
import flex.messaging.io.MapProxy;
import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;

public class AmfIgnoreSpringPropertyProxyTests {

    private static final Log log = LogFactory.getLog(AmfIgnoreSpringPropertyProxyTests.class);

    AmfMessageSerializer   serializer;
    AmfMessageDeserializer deserializer;

    AmfTrace serializerTrace;
    AmfTrace deserializerTrace;

    MockHttpServletResponse response;
    MockHttpServletRequest  request;

    @BeforeClass
    public static void ensureBeanProxyInitialization() {
        BeanProxy.addIgnoreProperty(ASObject.class, "type");
    }

    @Before
    public void setUp() {
        this.serializer = new AmfMessageSerializer();
        this.serializerTrace = new AmfTrace();
        this.response = new MockHttpServletResponse();
        this.serializer.initialize(new SerializationContext(), response.getOutputStream(), serializerTrace);
        this.deserializer = new AmfMessageDeserializer();
        this.deserializerTrace = new AmfTrace();
        this.request = new MockHttpServletRequest();
    }

    @After
    public void trace() throws UnsupportedEncodingException {
        log.info("Serializer Trace:\n" + serializerTrace);
        log.info("Deserializer Trace:\n" + deserializerTrace);
    }

    @Test
    public void serializeIgnoredProperties() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringPropertyProxy());
        IgnorablePropsObject data = new IgnorablePropsObject();
        data.setFoo("foo");
        data.setBar("bar");
        data.setBaz("baz");
        serialize(data);

        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, new ASObjectProxy());

        ASObject result = (ASObject) deserialize();

        Assert.assertTrue(!result.containsKey("foo"));
        Assert.assertTrue(!result.containsKey("fooField"));
        Assert.assertTrue(!result.containsKey("bar"));
        Assert.assertTrue(!result.containsKey("barField"));
        Assert.assertTrue(!result.containsKey("bazField"));
        Assert.assertEquals("baz", result.get("baz"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deserializeIgnoredProperties() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringPropertyProxy());
        ASObject data = new ASObject(IgnorablePropsObject.class.getName());
        data.put("foo", "foo");
        data.put("bar", "bar");
        data.put("baz", "baz");
        data.put("zoo", "zoo");
        serialize(data);

        IgnorablePropsObject result = (IgnorablePropsObject) deserialize();

        Assert.assertEquals("unset", result.getFoo());
        Assert.assertEquals("bar", result.getBar());
        Assert.assertEquals("unset", result.getBaz());
    }

    @Test
    public void serializeIgnoredFields() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringFieldProxy());
        IgnorablePropsObject data = new IgnorablePropsObject();
        data.setFoo("foo");
        data.setBar("bar");
        data.setBaz("baz");
        serialize(data);

        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, new ASObjectProxy());

        ASObject result = (ASObject) deserialize();

        Assert.assertTrue(!result.containsKey("foo"));
        Assert.assertTrue(!result.containsKey("fooField"));
        Assert.assertTrue(!result.containsKey("bar"));
        Assert.assertTrue(!result.containsKey("barField"));
        Assert.assertTrue(!result.containsKey("baz"));
        Assert.assertEquals("baz", result.get("bazField"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deserializeIgnoredFields() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringFieldProxy());
        ASObject data = new ASObject(IgnorablePropsObject.class.getName());
        data.put("fooField", "foo");
        data.put("barField", "bar");
        data.put("bazField", "baz");
        data.put("zooField", "zoo");
        serialize(data);

        IgnorablePropsObject result = (IgnorablePropsObject) deserialize();

        Assert.assertEquals("unset", result.getFoo());
        Assert.assertEquals("bar", result.getBar());
        Assert.assertEquals("unset", result.getBaz());
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

    private PropertyProxy createSpringPropertyProxy() {
        GenericConversionService cs = new GenericConversionService();
        cs.addConverter(new NumberConverter());
        SpringPropertyProxy voProxy = SpringPropertyProxy.proxyFor(IgnorablePropsObject.class, false, cs);
        return voProxy;
    }

    private PropertyProxy createSpringFieldProxy() {
        GenericConversionService cs = new GenericConversionService();
        cs.addConverter(new NumberConverter());
        SpringPropertyProxy voProxy = SpringPropertyProxy.proxyFor(IgnorablePropsObject.class, true, cs);
        return voProxy;
    }

    @SuppressWarnings("serial")
    public static final class ASObjectProxy extends MapProxy {
        @Override
        public Object createInstance(String className) {
            return super.createInstance(null);
        }
    }
}
