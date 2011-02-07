package org.springframework.flex.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.flex.core.io.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;


public class AmfViewTests extends TestCase {

    private MockHttpServletResponse response;

    private MockHttpServletRequest request;
    
    @Override
    protected void setUp() throws Exception {
        this.response = new MockHttpServletResponse();
        this.request = new MockHttpServletRequest();
    }
    
    public void testRenderSimpleStringModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("foo", "bar");
        AmfView view = new AmfView();
        view.render(model, request, response);
        
        Object result = deserialize();
        
        assertEquals(model, result);
    }
    
    @SuppressWarnings("unchecked")
	public void testRenderFullyTypedModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("person", Person.stubPerson());
        AmfView view = new AmfView();
        view.render(model, request, response);
        
        Map<String, Object> result = (Map<String, Object>) deserialize();
        assertTrue(result.containsKey("person"));
        assertTrue(result.get("person") instanceof Person);
    }
    
    private Object deserialize() throws ClassNotFoundException, IOException {
        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        this.request.setContent(this.response.getContentAsByteArray());
        deserializer.initialize(new SerializationContext(), this.request.getInputStream(), null);
        return deserializer.readObject();
    }
}
