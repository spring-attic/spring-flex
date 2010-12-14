
package org.springframework.flex.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "hibernate-jpa-context.xml")
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
    SpringPropertyProxyHibernateJPATests.InternalDbTestExecutionListener.class })
public class SpringPropertyProxyHibernateJPATests {

    private static final Log log = LogFactory.getLog(SpringPropertyProxyHibernateJPATests.class);

    @Autowired
    EntityManagerFactory emf;
    
    @PersistenceContext 
    EntityManager em;

    AmfMessageSerializer serializer;

    AmfMessageDeserializer deserializer;

    AmfTrace serializerTrace;

    AmfTrace deserializerTrace;

    MockHttpServletResponse response;

    MockHttpServletRequest request;
    
    boolean isTransactional = false;
    
    @BeforeTransaction
    public void setTxFlag() {
        isTransactional = true;
    }


    @Before
    public void init() throws Exception {
        JpaHibernateConfigProcessor processor = new JpaHibernateConfigProcessor();
        processor.setEntityManagerFactory(this.emf);
        processor.afterPropertiesSet();
        processor.processAfterStartup(null);
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
    public void testSerializationOutsideTransactionAfterFind() throws IOException, ClassNotFoundException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, 1);
        em.close();
        serialize(person);

        Person result = (Person) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.getId());
        assertEquals("Dad", result.getName());
        assertNotNull(result.getAddress());
        assertNull(result.getSpouse());
        assertNull(result.getChildren());
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterFind() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, 1);
        serialize(person);

        Person result = (Person) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.getId());
        assertEquals("Dad", result.getName());
        assertNotNull(result.getAddress());
        assertNull(result.getSpouse());
        assertNull(result.getChildren());
    }

    
    @Test
    public void testSerializationOutsideTransactionAfterFindAndInitializedCollection() throws IOException, ClassNotFoundException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, 1);
        person.getChildren().iterator();
        em.close();
        serialize(person);

        Person result = (Person) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.getId());
        assertEquals("Dad", result.getName());
        assertNotNull(result.getAddress());
        assertNull(result.getSpouse());
        assertNotNull(result.getChildren());
        assertEquals(2, result.getChildren().size());
        for (Person childResult : result.getChildren()) {
            assertNotNull(childResult.getId());
            assertTrue(StringUtils.hasText(childResult.getName()));
            assertNull(childResult.getSpouse());
            assertNull(childResult.getChildren());
        }
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterFindAndInitializedCollection() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, 1);
        person.getChildren().iterator();
        serialize(person);
        
        Person result = (Person) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.getId());
        assertEquals("Dad", result.getName());
        assertNotNull(result.getAddress());
        assertNull(result.getSpouse());
        assertNotNull(result.getChildren());
        assertEquals(2, result.getChildren().size());
        for (Person childResult : result.getChildren()) {
            assertNotNull(childResult.getId());
            assertTrue(StringUtils.hasText(childResult.getName()));
            assertNull(childResult.getSpouse());
            assertNull(childResult.getChildren());
        }
    }

    @Test
    public void testSerializationOutsideTransactionAfterGetReference() throws IOException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        Person person = em.getReference(Person.class, 1);
        em.close();

        try {
            serialize(person);
            fail("Expected a LazyInitializationException");
        } catch (LazyInitializationException ex) {
            // expected
        }
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterGetReference() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        Person person = em.getReference(Person.class, 1);
        serialize(person);
        
        Person result = (Person) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.getId());
        assertEquals("Dad", result.getName());
        assertNotNull(result.getAddress());
        assertNull(result.getSpouse());
        assertNull(result.getChildren());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializationOutsideTransactionAfterQuery() throws IOException, ClassNotFoundException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        List<Person> people = em.createQuery("from Person").getResultList();
        em.close();
        
        serialize(people);
        
        List<Person> results = (List<Person>) deserialize();
        
        for (Person result : results) {
            assertNotNull(result.getId());
            assertTrue(StringUtils.hasText(result.getName()));
            if(result.getSpouse() != null) {
                assertTrue(results.contains(result.getSpouse()));
            }
            assertNull(result.getChildren());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterHibernateQuery() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        List<Person> people = em.createQuery("from Person").getResultList();
        serialize(people);
        
        List<Person> results = (List<Person>) deserialize();
        
        for (Person result : results) {
            assertNotNull(result.getId());
            assertTrue(StringUtils.hasText(result.getName()));
            if(result.getSpouse() != null) {
                assertTrue(results.contains(result.getSpouse()));
            }
            assertNull(result.getChildren());
        }
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        Person person = new Person();
        person.setId(5000);
        person.setName("Bob");
        serialize(person);
        
        Person deserializedPerson = (Person) deserialize();
        assertEquals(new Integer(5000), deserializedPerson.getId());
        EntityManager em = getEntityManager();
        em.persist(deserializedPerson);
        assertNotNull(deserializedPerson.getId());
        assertTrue(deserializedPerson.getId() > 0 && deserializedPerson.getId() < 5000);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithPrimitiveNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setId(0);
        contactInfo.setEmail("bob@foo.com");
        contactInfo.setPhone("5555551234");
        serialize(contactInfo);
        
        ContactInfo deserializedContactInfo = (ContactInfo) deserialize();
        assertEquals(0, deserializedContactInfo.getId());
        EntityManager em = getEntityManager();
        em.persist(deserializedContactInfo);
        assertNotNull(deserializedContactInfo.getId());
        assertTrue(deserializedContactInfo.getId() > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithVersion() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        Person person = new Person();
        person.setId(5000);
        person.setVersion(-1);
        person.setName("Bob");
        serialize(person);
        
        Person deserializedPerson = (Person) deserialize();
        assertNull(deserializedPerson.getVersion());
        EntityManager em = getEntityManager();
        em.persist(deserializedPerson);
        assertNotNull(deserializedPerson.getId());
        assertTrue(deserializedPerson.getId() > 0 && deserializedPerson.getId() < 5000);
        assertNotNull(deserializedPerson.getVersion());
        assertTrue(deserializedPerson.getVersion() == 0);
        deserializedPerson.setName("Robert");
        em.flush();
        assertTrue(deserializedPerson.getVersion() > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setId(0);
        contactInfo.setVersion(-1);
        contactInfo.setEmail("bob@foo.com");
        contactInfo.setPhone("5555551234");
        serialize(contactInfo);
        
        ContactInfo deserializedContactInfo = (ContactInfo) deserialize();
        assertEquals(0, deserializedContactInfo.getId());
        EntityManager em = getEntityManager();
        em.persist(deserializedContactInfo);
        assertNotNull(deserializedContactInfo.getId());
        assertTrue(deserializedContactInfo.getId() > 0);
        assertTrue(deserializedContactInfo.getVersion() == 0);
        deserializedContactInfo.setEmail("bob@foobar.com");
        em.flush();
        assertTrue(deserializedContactInfo.getVersion() > 0);
    }
    
    private EntityManager getEntityManager() {
        if (!isTransactional){
            return emf.createEntityManager();
        } else {
            return this.em;
        }
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

    public static class InternalDbTestExecutionListener extends AbstractTestExecutionListener {

        public void beforeTestClass(TestContext testContext) throws Exception {
            EntityManagerFactory emf = testContext.getApplicationContext().getBean(EntityManagerFactory.class);
            EntityManager em = emf.createEntityManager();

            em.getTransaction().begin();
            Person father = new Person();
            father.setName("Dad");
            em.persist(father);
            
            Address address = new Address();
            address.setStreet("777 Techwood Drive");
            address.setCity("Atlanta");
            address.setState("GA");
            address.setZipcode("30022");
            address.setRooms(5);
            address.setMoveInDate(new Date());
            em.persist(address);
            
            father.setAddress(address);

            Person mother = new Person();
            mother.setName("Mom");
            mother.setSpouse(father);
            em.persist(mother);

            father.setSpouse(mother);

            Person child1 = new Person();
            child1.setName("Jack");
            em.persist(child1);

            Person daughterInLaw = new Person();
            daughterInLaw.setName("Lisa");
            daughterInLaw.setSpouse(child1);
            em.persist(daughterInLaw);

            child1.setSpouse(daughterInLaw);

            Person child2 = new Person();
            child2.setName("Jill");
            em.persist(child2);

            Set<Person> children = new HashSet<Person>();
            children.add(child1);
            children.add(child2);

            father.setChildren(children);
            mother.setChildren(children);

            em.flush();
            em.getTransaction().commit();
            em.close();
        }

    }

}
