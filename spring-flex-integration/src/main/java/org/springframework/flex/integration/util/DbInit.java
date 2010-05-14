package org.springframework.flex.integration.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.integration.domain.Address;
import org.springframework.flex.integration.domain.Person;
import org.springframework.stereotype.Component;

@Component
public class DbInit implements DbInitializer{

	SessionFactory sessionFactory;
	
	@Autowired
	public DbInit(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@PostConstruct
	public void init() {
		
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
