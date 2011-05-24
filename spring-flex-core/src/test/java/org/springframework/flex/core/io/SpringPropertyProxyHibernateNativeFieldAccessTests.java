
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.core.io.domain.AddressNP;
import org.springframework.flex.core.io.domain.Company;
import org.springframework.flex.core.io.domain.CompanyNP;
import org.springframework.flex.core.io.domain.ContactInfo;
import org.springframework.flex.core.io.domain.ContactInfoNP;
import org.springframework.flex.core.io.domain.PersonNP;
import org.springframework.flex.core.io.domain.PrimitiveCompany;
import org.springframework.flex.core.io.domain.PrimitiveCompanyNP;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.util.ToStringPrettyPrinter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "hibernate-np-context.xml")
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
    SpringPropertyProxyHibernateNativeFieldAccessTests.InternalDbTestExecutionListener.class })
public class SpringPropertyProxyHibernateNativeFieldAccessTests {

    private static final Log log = LogFactory.getLog(SpringPropertyProxyHibernateNativeFieldAccessTests.class);

    @Autowired
    SessionFactory sessionFactory;

    AmfMessageSerializer serializer;

    AmfMessageDeserializer deserializer;

    AmfTrace serializerTrace;

    AmfTrace deserializerTrace;

    MockHttpServletResponse response;

    MockHttpServletRequest request;

    @Before
    public void init() throws Exception {
        HibernateConfigProcessor processor = new HibernateConfigProcessor();
        processor.setSessionFactory(this.sessionFactory);
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
    public void testSerializationOutsideTransactionAfterHibernateGet() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        PersonNP person = (PersonNP) session.get(PersonNP.class, 1);
        session.close();
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
    public void testDebugLoggingOutsideTransactionAfterHibernateGet() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        PersonNP person = (PersonNP) session.get(PersonNP.class, 1);
        session.close();
        String result = log(person);
        log.info("Result from Logger:\n"+result);
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterHibernateGet() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        PersonNP person = (PersonNP) session.get(PersonNP.class, 1);
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
    public void testSerializationOutsideTransactionAfterHibernateGetAndInitializedCollection() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        PersonNP person = (PersonNP) session.get(PersonNP.class, 1);
        person.children.iterator();
        session.close();
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
    public void testSerializationInsideTransactionAfterHibernateGetAndInitializedCollection() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        PersonNP person = (PersonNP) session.get(PersonNP.class, 1);
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
    public void testSerializationOutsideTransactionAfterHibernateLoad() throws IOException {
        Session session = this.sessionFactory.openSession();
        PersonNP person = (PersonNP) session.load(PersonNP.class, 1);
        session.close();

        try {
            serialize(person);
            fail("Expected a LazyInitializationException");
        } catch (LazyInitializationException ex) {
            // expected
        }
    }

    @Test
    @Transactional
    public void testSerializationInsideTransactionAfterHibernateLoad() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        PersonNP person = (PersonNP) session.load(PersonNP.class, 1);
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
    public void testSerializationOutsideTransactionAfterHibernateQuery() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        List<PersonNP> people = session.createQuery("from PersonNP").list();
        session.close();
        
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
        Session session = this.sessionFactory.getCurrentSession();
        List<PersonNP> people = session.createQuery("from PersonNP").list();
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
    public void testDeserializeAndPersistNewEntityWithNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(PersonNP.class);
        PersonNP person = new PersonNP();
        proxy.setValue(person, "id", Double.NaN);
        proxy.setValue(person, "name", "Bob");
        
        assertEquals(null, person.id);
        Session session = this.sessionFactory.getCurrentSession();
        session.save(person);
        assertNotNull(person.id);
        assertTrue(person.id > 0);
    }
    
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithPrimitiveNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(ContactInfo.class);
        ContactInfoNP contactInfo = new ContactInfoNP();
        proxy.setValue(contactInfo, "id", 0);
        proxy.setValue(contactInfo, "email", "bob@foo.com");
        proxy.setValue(contactInfo, "phone", "5555551234");
        
        assertEquals(0, contactInfo.id);
        Session session = this.sessionFactory.getCurrentSession();
        session.save(contactInfo);
        assertNotNull(contactInfo.id);
        assertTrue(contactInfo.id > 0);
    }
    
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(PersonNP.class);
        PersonNP person = new PersonNP();
        proxy.setValue(person, "id", Double.NaN);
        proxy.setValue(person, "version", Double.NaN);
        proxy.setValue(person, "name", "Bob");
        
        assertNull(person.version);
        Session session = this.sessionFactory.getCurrentSession();
        session.save(person);
        assertNotNull(person.id);
        assertTrue(person.id > 0);
        assertNotNull(person.version);
        assertTrue(person.version == 0);
        person.name = "Robert";
        session.flush();
        assertTrue(person.version > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(ContactInfo.class);
        ContactInfoNP contactInfo = new ContactInfoNP();
        proxy.setValue(contactInfo, "id", 0);
        proxy.setValue(contactInfo, "version", 0);
        proxy.setValue(contactInfo, "email", "bob@foo.com");
        proxy.setValue(contactInfo, "phone", "5555551234");
        
        assertEquals(0, contactInfo.id);
        Session session = this.sessionFactory.getCurrentSession();
        session.save(contactInfo);
        assertNotNull(contactInfo.id);
        assertTrue(contactInfo.id > 0);
        assertTrue(contactInfo.version == 0);
        contactInfo.email = "bob@foobar.com";
        session.flush();
        assertTrue(contactInfo.version > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewXMLMappedEntityWithVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(Company.class);
        CompanyNP company = new CompanyNP();
        proxy.setValue(company, "id", Double.NaN);
        proxy.setValue(company, "version", Double.NaN);
        proxy.setValue(company, "name", "SpringSource");
        
        assertNull(company.version);
        Session session = this.sessionFactory.getCurrentSession();
        session.save(company);
        assertNotNull(company.id);
        assertTrue(company.id > 0);
        assertNotNull(company.version);
        assertTrue(company.version == 0);
        company.name = "VMware";
        session.flush();
        assertTrue(company.version > 0);
    }
    
    @Test
    @Transactional
    public void testDeserializeAndPersistNewXMLMappedEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(PrimitiveCompany.class);
        PrimitiveCompanyNP company = new PrimitiveCompanyNP();
        proxy.setValue(company, "id", 0);
        proxy.setValue(company, "version", 0);
        proxy.setValue(company, "name", "SpringSource");
        
        assertEquals(0, company.id);
        Session session = this.sessionFactory.getCurrentSession();
        session.save(company);
        assertNotNull(company.id);
        assertTrue(company.id > 0);
        assertTrue(company.version == 0);
        company.name = "VMware";
        session.flush();
        assertTrue(company.version > 0);
    }
    
    private void serialize(Object data) throws IOException {
        MessageBody body = new MessageBody();
        body.setData(data);
        serializer.writeBody(body);
    }
    
    private String log(Object data) {
        MessageBody body = new MessageBody();
        body.setData(data);
        ToStringPrettyPrinter printer = new ToStringPrettyPrinter();
        return printer.prettify(body);
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
            SessionFactory sessionFactory = testContext.getApplicationContext().getBean(SessionFactory.class);
            Session session = sessionFactory.openSession();

            PersonNP father = new PersonNP();
            father.name = "Dad";
            session.save(father);
            
            AddressNP address = new AddressNP();
            address.street = "777 Techwood Drive";
            address.city = "Atlanta";
            address.state = "GA";
            address.zipcode = "30022";
            address.rooms = 5;
            address.moveInDate = new Date();
            session.save(address);
            
            father.address = address;
            session.update(father);

            PersonNP mother = new PersonNP();
            mother.name = "Mom";
            mother.spouse = father;
            session.save(mother);

            father.spouse = mother;
            session.update(father);

            PersonNP child1 = new PersonNP();
            child1.name = "Jack";
            session.save(child1);

            PersonNP daughterInLaw = new PersonNP();
            daughterInLaw.name = "Lisa";
            daughterInLaw.spouse = child1;
            session.save(daughterInLaw);

            child1.spouse = daughterInLaw;
            session.update(child1);

            PersonNP child2 = new PersonNP();
            child2.name = "Jill";
            session.save(child2);

            Set<PersonNP> children = new HashSet<PersonNP>();
            children.add(child1);
            children.add(child2);

            father.children = children;
            mother.children = children;

            session.update(father);
            session.update(mother);

            session.flush();
            session.close();
        }

    }

}
