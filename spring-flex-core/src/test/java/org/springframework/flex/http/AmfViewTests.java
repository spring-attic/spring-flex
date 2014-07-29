package org.springframework.flex.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class AmfViewTests {

    private MockHttpServletResponse response;

    private MockHttpServletRequest request;
    
    @Before
    public void setUp() throws Exception {
        this.response = new MockHttpServletResponse();
        this.request = new MockHttpServletRequest();
    }

    @Test
    public void renderSimpleStringModel() throws Exception {
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
