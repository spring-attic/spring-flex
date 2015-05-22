package org.springframework.flex.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.flex.core.io.domain.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;

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

    @Test
    public void renderFullyTypedModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("person", Person.stubPerson());
        AmfView view = new AmfView();
        view.render(model, request, response);

        Person result = (Person) deserialize();
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void renderFullyTypedMultiValuedModel() throws Exception {
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
