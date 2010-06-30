package org.example.flexrocks.util;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.example.flexrocks.domain.Person;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

@Component
public class DbInit implements BeanFactoryAware {
	
	BeanFactory beanFactory;
	
	@PostConstruct
	public void init() {
		EntityManagerFactory emf = beanFactory.getBean(EntityManagerFactory.class);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        
        Person father = new Person();
        father.setName("Dad");
        em.persist(father);
        
        //father.setAddress(address);

        Person mother = new Person();
        mother.setName("Mom");
        //mother.setSpouse(father);
        em.persist(mother);

        //father.setSpouse(mother);
        //session.update(father);

        Person child1 = new Person();
        child1.setName("Jack");
        em.persist(child1);

        Person daughterInLaw = new Person();
        daughterInLaw.setName("Lisa");
        //daughterInLaw.setSpouse(child1);
        em.persist(daughterInLaw);

        //child1.setSpouse(daughterInLaw);
        //session.update(child1);

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

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
