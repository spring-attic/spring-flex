package org.springframework.flex.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.domain.IgnorablePropsObject;
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
        
    AmfMessageSerializer serializer;

    AmfMessageDeserializer deserializer;

    AmfTrace serializerTrace;

    AmfTrace deserializerTrace;
    
    MockHttpServletResponse response;

    MockHttpServletRequest request;
    
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
    public void testSerializeIgnoredProperties() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringPropertyProxy());
        IgnorablePropsObject data = new IgnorablePropsObject();
        data.setFoo("foo");
        data.setBar("bar");
        data.setBaz("baz");
        serialize(data);
        
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, new ASObjectProxy());
        
        ASObject result = (ASObject) deserialize();
        
        assertTrue(!result.containsKey("foo"));
        assertTrue(!result.containsKey("fooField"));
        assertTrue(!result.containsKey("bar"));
        assertTrue(!result.containsKey("barField"));
        assertTrue(!result.containsKey("bazField"));
        assertEquals("baz", result.get("baz"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDeserializeIgnoredProperties() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringPropertyProxy());
        ASObject data = new ASObject(IgnorablePropsObject.class.getName());
        data.put("foo", "foo");
        data.put("bar", "bar");
        data.put("baz", "baz");
        serialize(data);
        
        IgnorablePropsObject result = (IgnorablePropsObject) deserialize();
        
        assertEquals("unset", result.getFoo());
        assertEquals("bar", result.getBar());
        assertEquals("unset", result.getBaz());
    }
    
    @Test
    public void testSerializeIgnoredFields() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringFieldProxy());
        IgnorablePropsObject data = new IgnorablePropsObject();
        data.setFoo("foo");
        data.setBar("bar");
        data.setBaz("baz");
        serialize(data);
        
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, new ASObjectProxy());
        
        ASObject result = (ASObject) deserialize();
        
        assertTrue(!result.containsKey("foo"));
        assertTrue(!result.containsKey("fooField"));
        assertTrue(!result.containsKey("bar"));
        assertTrue(!result.containsKey("barField"));
        assertTrue(!result.containsKey("baz"));
        assertEquals("baz", result.get("bazField"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDeserializeIgnoredFields() throws IOException, ClassNotFoundException {
        PropertyProxyRegistry.getRegistry().register(IgnorablePropsObject.class, createSpringFieldProxy());
        ASObject data = new ASObject(IgnorablePropsObject.class.getName());
        data.put("fooField", "foo");
        data.put("barField", "bar");
        data.put("bazField", "baz");
        serialize(data);
        
        IgnorablePropsObject result = (IgnorablePropsObject) deserialize();
        
        assertEquals("unset", result.getFoo());
        assertEquals("bar", result.getBar());
        assertEquals("unset", result.getBaz());
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
        SpringPropertyProxy voProxy = new SpringPropertyProxy(IgnorablePropsObject.class, false);
        voProxy.setConversionService(cs);
        return voProxy;
    }
    
    private PropertyProxy createSpringFieldProxy() {
        GenericConversionService cs = new GenericConversionService();
        cs.addConverter(new NumberConverter());
        SpringPropertyProxy voProxy = new SpringPropertyProxy(IgnorablePropsObject.class, true);
        voProxy.setConversionService(cs);
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
