package org.springframework.flex.hibernate3;

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
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.hibernate3.domain.Address;
import org.springframework.flex.hibernate3.domain.Company;
import org.springframework.flex.hibernate3.domain.ContactInfo;
import org.springframework.flex.hibernate3.domain.Person;
import org.springframework.flex.hibernate3.domain.PrimitiveCompany;
import org.springframework.flex.hibernate3.config.HibernateConfigProcessor;
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
@ContextConfiguration(locations = "hibernate-context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class,
                                SpringPropertyProxyHibernateNativeTests.InternalDbTestExecutionListener.class})
public class SpringPropertyProxyHibernateNativeTests {

    private static final Log log = LogFactory.getLog(SpringPropertyProxyHibernateNativeTests.class);

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
    public void serializationOutsideTransactionAfterHibernateGet() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        Person person = (Person) session.get(Person.class, 1);
        session.close();
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
    public void debugLoggingOutsideTransactionAfterHibernateGet() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        Person person = (Person) session.get(Person.class, 1);
        session.close();
        String result = log(person);
        log.info("Result from Logger:\n" + result);
    }

    @Test
    @Transactional
    public void serializationInsideTransactionAfterHibernateGet() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        Person person = (Person) session.get(Person.class, 1);
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
    public void serializationOutsideTransactionAfterHibernateGetAndInitializedCollection() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        Person person = (Person) session.get(Person.class, 1);
        person.getChildren().iterator();
        session.close();
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
    public void serializationInsideTransactionAfterHibernateGetAndInitializedCollection() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        Person person = (Person) session.get(Person.class, 1);
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
    public void serializationOutsideTransactionAfterHibernateLoad() throws IOException {
        Session session = this.sessionFactory.openSession();
        Person person = (Person) session.load(Person.class, 1);
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
    public void serializationInsideTransactionAfterHibernateLoad() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        Person person = (Person) session.load(Person.class, 1);
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
    public void serializationOutsideTransactionAfterHibernateQuery() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.openSession();
        List<Person> people = session.createQuery("from Person").list();
        session.close();

        serialize(people);

        List<Person> results = (List<Person>) deserialize();

        for (Person result : results) {
            assertNotNull(result.getId());
            assertTrue(StringUtils.hasText(result.getName()));
            if (result.getSpouse() != null) {
                assertTrue(results.contains(result.getSpouse()));
            }
            assertNull(result.getChildren());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void serializationInsideTransactionAfterHibernateQuery() throws IOException, ClassNotFoundException {
        Session session = this.sessionFactory.getCurrentSession();
        List<Person> people = session.createQuery("from Person").list();
        serialize(people);

        List<Person> results = (List<Person>) deserialize();

        for (Person result : results) {
            assertNotNull(result.getId());
            assertTrue(StringUtils.hasText(result.getName()));
            if (result.getSpouse() != null) {
                assertTrue(results.contains(result.getSpouse()));
            }
            assertNull(result.getChildren());
        }
    }

    @Test
    @Transactional
    public void deserializeAndPersistNewEntityWithNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(Person.class);
        Person person = new Person();
        proxy.setValue(person, "id", Double.NaN);
        proxy.setValue(person, "name", "Bob");

        assertEquals(null, person.getId());
        Session session = this.sessionFactory.getCurrentSession();
        session.save(person);
        assertNotNull(person.getId());
        assertTrue(person.getId() > 0);
    }

    @Test
    @Transactional
    public void deserializeAndPersistNewEntityWithPrimitiveNumericAutogeneratedId() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(ContactInfo.class);
        ContactInfo contactInfo = new ContactInfo();
        proxy.setValue(contactInfo, "id", 0);
        proxy.setValue(contactInfo, "email", "bob@foo.com");
        proxy.setValue(contactInfo, "phone", "5555551234");

        assertEquals(0, contactInfo.getId());
        Session session = this.sessionFactory.getCurrentSession();
        session.save(contactInfo);
        assertNotNull(contactInfo.getId());
        assertTrue(contactInfo.getId() > 0);
    }

    @Test
    @Transactional
    public void deserializeAndPersistNewEntityWithVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(Person.class);
        Person person = new Person();
        proxy.setValue(person, "id", Double.NaN);
        proxy.setValue(person, "version", Double.NaN);
        proxy.setValue(person, "name", "Bob");

        assertNull(person.getVersion());
        Session session = this.sessionFactory.getCurrentSession();
        session.save(person);
        assertNotNull(person.getId());
        assertTrue(person.getId() > 0);
        assertNotNull(person.getVersion());
        assertTrue(person.getVersion() == 0);
        person.setName("Robert");
        session.flush();
        assertTrue(person.getVersion() > 0);
    }

    @Test
    @Transactional
    public void deserializeAndPersistNewEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(ContactInfo.class);
        ContactInfo contactInfo = new ContactInfo();
        proxy.setValue(contactInfo, "id", 0);
        proxy.setValue(contactInfo, "version", 0);
        proxy.setValue(contactInfo, "email", "bob@foo.com");
        proxy.setValue(contactInfo, "phone", "5555551234");

        assertEquals(0, contactInfo.getId());
        Session session = this.sessionFactory.getCurrentSession();
        session.save(contactInfo);
        assertNotNull(contactInfo.getId());
        assertTrue(contactInfo.getId() > 0);
        assertTrue(contactInfo.getVersion() == 0);
        contactInfo.setEmail("bob@foobar.com");
        session.flush();
        assertTrue(contactInfo.getVersion() > 0);
    }

    @Test
    @Transactional
    public void deserializeAndPersistNewXMLMappedEntityWithVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(Company.class);
        Company company = new Company();
        proxy.setValue(company, "id", Double.NaN);
        proxy.setValue(company, "version", Double.NaN);
        proxy.setValue(company, "name", "SpringSource");

        assertNull(company.getVersion());
        Session session = this.sessionFactory.getCurrentSession();
        session.save(company);
        assertNotNull(company.getId());
        assertTrue(company.getId() > 0);
        assertNotNull(company.getVersion());
        assertTrue(company.getVersion() == 0);
        company.setName("VMware");
        session.flush();
        assertTrue(company.getVersion() > 0);
    }

    @Test
    @Transactional
    public void deserializeAndPersistNewXMLMappedEntityWithPrimitiveVersion() throws IOException, ClassNotFoundException {
        SpringPropertyProxy proxy = (SpringPropertyProxy) PropertyProxyRegistry.getRegistry().getProxy(PrimitiveCompany.class);
        PrimitiveCompany company = new PrimitiveCompany();
        proxy.setValue(company, "id", 0);
        proxy.setValue(company, "version", 0);
        proxy.setValue(company, "name", "SpringSource");

        assertEquals(0, company.getId());
        Session session = this.sessionFactory.getCurrentSession();
        session.save(company);
        assertNotNull(company.getId());
        assertTrue(company.getId() > 0);
        assertTrue(company.getVersion() == 0);
        company.setName("VMware");
        session.flush();
        assertTrue(company.getVersion() > 0);
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

            Person father = new Person();
            father.setName("Dad");
            session.save(father);

            Address address = new Address();
            address.setStreet("777 Techwood Drive");
            address.setCity("Atlanta");
            address.setState("GA");
            address.setZipcode("30022");
            address.setRooms(5);
            address.setMoveInDate(new Date());
            session.save(address);

            father.setAddress(address);
            session.update(father);

            Person mother = new Person();
            mother.setName("Mom");
            mother.setSpouse(father);
            session.save(mother);

            father.setSpouse(mother);
            session.update(father);

            Person child1 = new Person();
            child1.setName("Jack");
            session.save(child1);

            Person daughterInLaw = new Person();
            daughterInLaw.setName("Lisa");
            daughterInLaw.setSpouse(child1);
            session.save(daughterInLaw);

            child1.setSpouse(daughterInLaw);
            session.update(child1);

            Person child2 = new Person();
            child2.setName("Jill");
            session.save(child2);

            Set<Person> children = new HashSet<Person>();
            children.add(child1);
            children.add(child2);

            father.setChildren(children);
            mother.setChildren(children);

            session.update(father);
            session.update(mother);

            session.flush();
            session.close();
        }

    }

}
