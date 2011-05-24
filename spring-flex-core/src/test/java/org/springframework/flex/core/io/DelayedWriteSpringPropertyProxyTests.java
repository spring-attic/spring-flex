package org.springframework.flex.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.flex.core.io.domain.Address;
import org.springframework.flex.core.io.domain.ImmutableValueObject;
import org.springframework.flex.core.io.domain.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;



public class DelayedWriteSpringPropertyProxyTests {

    AmfMessageSerializer serializer;

    AmfMessageDeserializer deserializer;

    AmfTrace serializerTrace;

    AmfTrace deserializerTrace;
    
    MockHttpServletResponse response;

    MockHttpServletRequest request;
    
    @Before
    public void setUp() {
        GenericConversionService cs = new GenericConversionService();
        cs.addConverter(new NumberConverter());
        DelayedWriteSpringPropertyProxy voProxy = new DelayedWriteSpringPropertyProxy(ImmutableValueObject.class, false);
        voProxy.setConversionService(cs);
        PropertyProxyRegistry.getRegistry().register(ImmutableValueObject.class, voProxy);
        SpringPropertyProxy personProxy = new SpringPropertyProxy(Person.class, false);
        personProxy.setConversionService(cs);
        PropertyProxyRegistry.getRegistry().register(Person.class, personProxy);
        SpringPropertyProxy addressProxy = new SpringPropertyProxy(Address.class, false);
        addressProxy.setConversionService(cs);
        PropertyProxyRegistry.getRegistry().register(Address.class, addressProxy);
        
        this.serializer = new AmfMessageSerializer();
        this.serializerTrace = new AmfTrace();
        this.response = new MockHttpServletResponse();
        this.serializer.initialize(new SerializationContext(), response.getOutputStream(), serializerTrace);
        this.deserializer = new AmfMessageDeserializer();
        this.deserializerTrace = new AmfTrace();
        this.request = new MockHttpServletRequest();
    }
    
    @Test
    public void testDeserializeAnnotatedImmutableObject() throws IOException, ClassNotFoundException {
        ImmutableValueObject data = new ImmutableValueObject("bar", new Integer(1));
        data.setPersonRef(createPerson());
        data.setVoRef(new ImmutableValueObject("zed", new Integer(5)));
        serialize(data);
        
        ImmutableValueObject result = (ImmutableValueObject) deserialize();
        
        assertEquals("bar", result.getFoo());
        assertEquals(new Integer(1), result.getZoo());
        assertNotNull(result.getPersonRef());
        assertNotNull(result.getVoRef());
        assertEquals("zed", result.getVoRef().getFoo());
        assertEquals(new Integer(5), result.getVoRef().getZoo());
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
    
    private Person createPerson() {
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
