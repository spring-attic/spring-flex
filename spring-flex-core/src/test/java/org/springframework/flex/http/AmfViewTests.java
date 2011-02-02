package org.springframework.flex.http;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.flex.core.io.Address;
import org.springframework.flex.core.io.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;


public class AmfViewTests extends TestCase {

    MockHttpServletResponse response;

    MockHttpServletRequest request;
    
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
    
    public void testRenderFullyTypedModel() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("person", StubFactory.getPerson());
        AmfView view = new AmfView();
        view.render(model, request, response);
        
        deserialize();
    }
    
    private Object deserialize() throws ClassNotFoundException, IOException {
        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        this.request.setContent(this.response.getContentAsByteArray());
        deserializer.initialize(new SerializationContext(), this.request.getInputStream(), null);
        return deserializer.readObject();
    }
    
    private static final class StubFactory {
        
        public static Person getPerson() {
            Person father = new Person();
            father.setName("Dad");
            
            Address address = new Address();
            address.setStreet("777 Techwood Drive");
            address.setCity("Atlanta");
            address.setState("GA");
            address.setZipcode("30022");
            address.setRooms(5);
            address.setMoveInDate(new Date());
            
            father.setAddress(address);         

            Person mother = new Person();
            mother.setName("Mom");
            mother.setSpouse(father);

            father.setSpouse(mother);

            Person child1 = new Person();
            child1.setName("Jack");

            Person daughterInLaw = new Person();
            daughterInLaw.setName("Lisa");
            daughterInLaw.setSpouse(child1);

            child1.setSpouse(daughterInLaw);

            Person child2 = new Person();
            child2.setName("Jill");

            Set<Person> children = new HashSet<Person>();
            children.add(child1);
            children.add(child2);

            father.setChildren(children);
            mother.setChildren(children);
            
            return father;
        }
    }
}
