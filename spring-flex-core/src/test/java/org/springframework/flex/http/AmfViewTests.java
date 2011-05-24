package org.springframework.flex.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.flex.core.io.domain.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;


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
        
        assertEquals("bar", result);
    }
    
	public void testRenderFullyTypedModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("person", Person.stubPerson());
        AmfView view = new AmfView();
        view.render(model, request, response);
        
        Person result = (Person) deserialize();
        assertNotNull(result);
    }
	
	@SuppressWarnings("unchecked")
	public void testRenderFullyTypedMultiValuedModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("person1", Person.stubPerson());
        model.put("person2", Person.stubPerson());
        AmfView view = new AmfView();
        view.render(model, request, response);
        
        Map<String, Object> result = (Map<String, Object>) deserialize();
        assertNotNull(result);
        assertTrue(result.containsKey("person1"));
        assertTrue(result.get("person1") instanceof Person);
        assertTrue(result.containsKey("person2"));
        assertTrue(result.get("person2") instanceof Person);
    }
    
    private Object deserialize() throws ClassNotFoundException, IOException {
        Amf3Input deserializer = new Amf3Input(new SerializationContext());
        this.request.setContent(this.response.getContentAsByteArray());
        deserializer.setInputStream(this.request.getInputStream());
        return deserializer.readObject();
    }
}
