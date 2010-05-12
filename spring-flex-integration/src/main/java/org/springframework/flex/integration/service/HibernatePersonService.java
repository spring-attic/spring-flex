package org.springframework.flex.integration.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.integration.domain.Person;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RemotingDestination
@Repository
public class HibernatePersonService implements PersonService {

	private final SessionFactory sessionFactory;
	
	@Autowired
	public HibernatePersonService(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Transactional
	public Person loadPerson(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		return (Person) session.load(Person.class, id);
	}

}
