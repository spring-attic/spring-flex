
package org.springframework.flex.hibernate4.core.io;

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
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.hibernate4.core.io.domain.AddressNP;
import org.springframework.flex.hibernate4.core.io.domain.ContactInfoNP;
import org.springframework.flex.hibernate4.core.io.domain.EmbeddedAddressNP;
import org.springframework.flex.hibernate4.core.io.domain.EmbeddedFloorAttributesNP;
import org.springframework.flex.hibernate4.core.io.domain.EmbeddedFloorNP;
import org.springframework.flex.hibernate4.core.io.domain.PersonNP;
import org.springframework.flex.orm.hibernate4.config.JpaHibernateConfigProcessor;
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

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "hibernate-jpa-np-context.xml")
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
    SpringPropertyProxyHibernateJPAFieldAccessTests.InternalDbTestExecutionListener.class })
public class SpringPropertyProxyHibernateJPAFieldAccessTests {

    private static final Log log = LogFactory.getLog(SpringPropertyProxyHibernateJPAFieldAccessTests.class);

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
        processor.setUseDirectFieldAccess(true);
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
        PersonNP person = em.find(PersonNP.class, 1);
        em.close();
        serialize(person);

        PersonNP result = (PersonNP) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.id);
        assertEquals("Dad", result.name);
        assertNotNull(result.address);
        assertNull(result.spouse);
        assertNull(result.children);
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterFind() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        PersonNP person = em.find(PersonNP.class, 1);
        serialize(person);

        PersonNP result = (PersonNP) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.id);
        assertEquals("Dad", result.name);
        assertNotNull(result.address);
        assertNull(result.spouse);
        assertNull(result.children);
    }

    
    @Test
    public void testSerializationOutsideTransactionAfterFindAndInitializedCollection() throws IOException, ClassNotFoundException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        PersonNP person = em.find(PersonNP.class, 1);
        person.children.iterator();
        em.close();
        serialize(person);

        PersonNP result = (PersonNP) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.id);
        assertEquals("Dad", result.name);
        assertNotNull(result.address);
        assertNull(result.spouse);
        assertNotNull(result.children);
        assertEquals(2, result.children.size());
        for (PersonNP childResult : result.children) {
            assertNotNull(childResult.id);
            assertTrue(StringUtils.hasText(childResult.name));
            assertNull(childResult.spouse);
            assertNull(childResult.children);
        }
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterFindAndInitializedCollection() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        PersonNP person = em.find(PersonNP.class, 1);
        person.children.iterator();
        serialize(person);
        
        PersonNP result = (PersonNP) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.id);
        assertEquals("Dad", result.name);
        assertNotNull(result.address);
        assertNull(result.spouse);
        assertNotNull(result.children);
        assertEquals(2, result.children.size());
        for (PersonNP childResult : result.children) {
            assertNotNull(childResult.id);
            assertTrue(StringUtils.hasText(childResult.name));
            assertNull(childResult.spouse);
            assertNull(childResult.children);
        }
    }

    @Test
    public void testSerializationOutsideTransactionAfterGetReference() throws IOException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        PersonNP person = em.getReference(PersonNP.class, 1);
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
        PersonNP person = em.getReference(PersonNP.class, 1);
        serialize(person);
        
        PersonNP result = (PersonNP) deserialize();

        assertNotSame(person, result);
        assertEquals(new Integer(1), result.id);
        assertEquals("Dad", result.name);
        assertNotNull(result.address);
        assertNull(result.spouse);
        assertNull(result.children);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializationOutsideTransactionAfterQuery() throws IOException, ClassNotFoundException {
        assertTrue(!isTransactional);
        EntityManager em = getEntityManager();
        List<PersonNP> people = em.createQuery("from PersonNP").getResultList();
        em.close();
        
        serialize(people);
        
        List<PersonNP> results = (List<PersonNP>) deserialize();
        
        for (PersonNP result : results) {
            assertNotNull(result.id);
            assertTrue(StringUtils.hasText(result.name));
            if(result.spouse != null) {
                assertTrue(results.contains(result.spouse));
            }
            assertNull(result.children);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterHibernateQuery() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        EntityManager em = getEntityManager();
        List<PersonNP> people = em.createQuery("from PersonNP").getResultList();
        serialize(people);
        
        List<PersonNP> results = (List<PersonNP>) deserialize();
        
        for (PersonNP result : results) {
            assertNotNull(result.id);
            assertTrue(StringUtils.hasText(result.name));
            if(result.spouse != null) {
                assertTrue(results.contains(result.spouse));
            }
            assertNull(result.children);
        }
    }
    
    @Test
    @Transactional
    public void testSetValueAndPersistNewEntityWithNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(PersonNP.class);
        PersonNP person = new PersonNP();
        proxy.setValue(person, "id", Double.NaN);
        proxy.setValue(person, "name", "Bob");
        
        assertEquals(null, person.id);
        EntityManager em = getEntityManager();
        em.persist(person);
        assertNotNull(person.id);
        assertTrue(person.id > 0);
    }
    
    @Test
    public void testSerializeNewEntityWithNumericNullId() throws IOException, ClassNotFoundException {
        PersonNP person = new PersonNP();
        person.id = null;
        person.name = "Bob";
        serialize(person);
        
        PersonNP result = (PersonNP) deserialize();
        assertEquals(null, result.id);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithPrimitiveNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(ContactInfoNP.class);
        ContactInfoNP contactInfo = new ContactInfoNP();
        proxy.setValue(contactInfo, "id", 0);
        proxy.setValue(contactInfo, "email", "bob@foo.com");
        proxy.setValue(contactInfo, "phone", "5555551234");

        assertEquals(0, contactInfo.id);
        EntityManager em = getEntityManager();
        em.persist(contactInfo);
        assertNotNull(contactInfo.id);
        assertTrue(contactInfo.id > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithVersion() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(PersonNP.class);
        PersonNP person = new PersonNP();
        proxy.setValue(person, "id", Double.NaN);
        proxy.setValue(person, "version", Double.NaN);
        proxy.setValue(person, "name", "Bob");
        
        assertNull(person.version);
        EntityManager em = getEntityManager();
        em.persist(person);
        assertNotNull(person.id);
        assertTrue(person.id > 0);
        assertNotNull(person.version);
        assertTrue(person.version == 0);
        person.name = "Robert";
        em.flush();
        assertTrue(person.version > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(ContactInfoNP.class);
        ContactInfoNP contactInfo = new ContactInfoNP();
        proxy.setValue(contactInfo, "id", 0);
        proxy.setValue(contactInfo, "version", 0);
        proxy.setValue(contactInfo, "email", "bob@foo.com");
        proxy.setValue(contactInfo, "phone", "5555551234");
        
        assertEquals(0, contactInfo.id);
        EntityManager em = getEntityManager();
        em.persist(contactInfo);
        assertNotNull(contactInfo.id);
        assertTrue(contactInfo.id > 0);
        assertTrue(contactInfo.version == 0);
        contactInfo.email = "bob@foobar.com";
        em.flush();
        assertTrue(contactInfo.version > 0);
    }
    
    @Test
    @Transactional
    public void testPersistNewEntityWithVersion() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        PersonNP person = new PersonNP();
        person.id = 5000;
        person.version = null;
        person.name = "Bob";
        
        EntityManager em = getEntityManager();
        em.persist(person);
        assertNotNull(person.id);
        assertTrue(person.id > 0 && person.id < 5000);
        assertNotNull(person.version);
        assertTrue(person.version == 0);
        person.name = "Robert";
        em.flush();
        assertTrue(person.version > 0);
    }
    
    @Test
    @Transactional
    public void testPersistNewEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        assertTrue(isTransactional);
        ContactInfoNP contactInfo = new ContactInfoNP();
        contactInfo.id = 0;
        contactInfo.version = -1;
        contactInfo.email = "bob@foo.com";
        contactInfo.phone = "5555551234";
        serialize(contactInfo);
        
        ContactInfoNP deserializedContactInfoNP = (ContactInfoNP) deserialize();
        assertEquals(0, deserializedContactInfoNP.id);
        EntityManager em = getEntityManager();
        em.persist(deserializedContactInfoNP);
        assertNotNull(deserializedContactInfoNP.id);
        assertTrue(deserializedContactInfoNP.id > 0);
        assertTrue(deserializedContactInfoNP.version == 0);
        deserializedContactInfoNP.email = "bob@foobar.com";
        em.flush();
        assertTrue(deserializedContactInfoNP.version > 0);
    }
    
    @Test
    public void testPropertyProxyRegisteredForEmbeddedClass() {
    	PropertyProxy proxy = PropertyProxyRegistry.getRegistry().getProxy(EmbeddedAddressNP.class);
    	assertNotNull(proxy);
    	assertTrue(proxy instanceof SpringPropertyProxy);
    }
    
    @Test
    public void testPropertyProxyRegisteredForElementCollection() {
        PropertyProxy proxy = PropertyProxyRegistry.getRegistry().getProxy(EmbeddedFloorNP.class);
        assertNotNull(proxy);
        assertTrue(proxy instanceof SpringPropertyProxy);
    }
    
    @Test
    public void testPropertyProxyRegisteredForNestedEmbeddable() {
        PropertyProxy proxy = PropertyProxyRegistry.getRegistry().getProxy(EmbeddedFloorAttributesNP.class);
        assertNotNull(proxy);
        assertTrue(proxy instanceof SpringPropertyProxy);
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
            PersonNP father = new PersonNP();
            father.name = "Dad";
            em.persist(father);
            
            AddressNP address = new AddressNP();
            address.street = "777 Techwood Drive";
            address.city = "Atlanta";
            address.state = "GA";
            address.zipcode = "30022";
            address.rooms = 5;
            address.moveInDate = new Date();
            em.persist(address);
            
            father.address = address;

            PersonNP mother = new PersonNP();
            mother.name = "Mom";
            mother.spouse = father;
            em.persist(mother);

            father.spouse = mother;

            PersonNP child1 = new PersonNP();
            child1.name = "Jack";
            em.persist(child1);

            PersonNP daughterInLaw = new PersonNP();
            daughterInLaw.name = "Lisa";
            daughterInLaw.spouse = child1;
            em.persist(daughterInLaw);

            child1.spouse = daughterInLaw;

            PersonNP child2 = new PersonNP();
            child2.name = "Jill";
            em.persist(child2);

            Set<PersonNP> children = new HashSet<PersonNP>();
            children.add(child1);
            children.add(child2);

            father.children = children;
            mother.children = children;

            em.flush();
            em.getTransaction().commit();
            em.close();
        }

    }

}
