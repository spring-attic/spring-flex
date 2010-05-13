package org.springframework.flex.core.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageBody;

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration(locations="hibernate-context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, SpringPropertyProxyTests.InternalDbTestExecutionListener.class})
public class SpringPropertyProxyTests {

    private static final Log log = LogFactory.getLog(SpringPropertyProxyTests.class);
    @Autowired
    SessionFactory sessionFactory;
    
    AmfMessageSerializer serializer;
    
    AmfTrace trace;
    
    @Before
    public void init() {
        SpringPropertyProxy proxy = new SpringPropertyProxy(Person.class);
        GenericConversionService cs = new GenericConversionService();
        cs.addConverterFactory(new HibernateProxyInitializingConverterFactory());
        cs.addConverterFactory(new DefaultPersistentCollectionConverterFactory());
        proxy.setConversionService(cs);
        PropertyProxyRegistry.getRegistry().register(Person.class, proxy);
        this.serializer = new AmfMessageSerializer();
        this.trace = new AmfTrace();
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        this.serializer.initialize(new SerializationContext(), response.getOutputStream(), trace);
    }
    
    @After
    public void trace() {
        log.info(trace);
    }
    
    @Test
    @Ignore
    //@Transactional
    public void testSerializationWithSpringPropertyProxyAfterHibernateGet() throws IOException{
        Session session = this.sessionFactory.openSession();
        Person person = (Person) session.get(Person.class, 1);
        session.close();
        MessageBody body = new MessageBody();
        body.setData(person);
        serializer.writeBody(body);
        
        //TODO - going to need to implement a HibernateConversionService that uses TypeDescriptors that will not force lazy initialization
    }
    
    @Test
    @Transactional
    @Ignore
    public void testSerializationWithSpringPropertyProxyAfterHibernateGetAndInitializedCollection() throws IOException{
        Session session = this.sessionFactory.getCurrentSession();
        Person person = (Person) session.get(Person.class, 1);
        person.getChildren().iterator();
        MessageBody body = new MessageBody();
        body.setData(person);
        serializer.writeBody(body);
    }
    
    @Test
    @Transactional
    @Ignore
    public void testSerializationWithSpringPropertyProxyAfterHibernateLoad() throws IOException{
        Session session = this.sessionFactory.getCurrentSession();
        Person person = (Person) session.load(Person.class, 1);
        MessageBody body = new MessageBody();
        body.setData(person);
        serializer.writeBody(body);
    }
    
    public static class InternalDbTestExecutionListener extends AbstractTestExecutionListener {

        //private DataSource dataSource;
        
        public void beforeTestClass(TestContext testContext) throws Exception {
            SessionFactory sessionFactory = testContext.getApplicationContext().getBean(SessionFactory.class);
            Session session = sessionFactory.openSession();
            
            Person father = new Person();
            father.setName("Dad");
            Integer id = (Integer) session.save(father);
            System.out.println("Dad's ID: "+id);
            
            Person mother = new Person();
            mother.setName("Mom");
            mother.setSpouse(father);
            session.save(mother);
            
            father.setSpouse(mother);
            session.update(father);
            
            Person child1 = new Person();
            child1.setName("Jack");
            session.save(child1);
            
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

        /*public void afterTestClass(TestContext testContext) throws Exception {
            DataSource dataSource = testContext.getApplicationContext().get
            JdbcTemplate template = new JdbcTemplate(dataSource);
        }*/
        
        
    }

}
