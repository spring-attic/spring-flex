package org.springframework.flex.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;

import junit.framework.TestCase;


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
    
    private Object deserialize() throws ClassNotFoundException, IOException {
        Amf3Input deserializer = new Amf3Input(new SerializationContext());
        this.request.setContent(this.response.getContentAsByteArray());
        deserializer.setInputStream(this.request.getInputStream());
        return deserializer.readObject();
    }
}
