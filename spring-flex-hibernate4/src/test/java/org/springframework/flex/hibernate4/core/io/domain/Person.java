
package org.springframework.flex.hibernate4.core.io.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Version
    @Column(name = "version")
    private Integer version;

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    private Person spouse;

    @OneToOne
    private Address address;

    @OneToMany
    private Set<Address> previousAddresses;

    @ManyToMany
    private Set<Person> children;

    private MaritalStatus maritalStatus;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getSpouse() {
        return spouse;
    }

    public void setSpouse(Person spouse) {
        this.spouse = spouse;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Set<Address> getPreviousAddresses() {
        return previousAddresses;
    }

    public void setPreviousAddresses(Set<Address> previousAddresses) {
        this.previousAddresses = previousAddresses;
    }

    public Set<Person> getChildren() {
        return children;
    }

    public void setChildren(Set<Person> children) {
        this.children = children;
    }
    
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public static Person stubPerson() {
        Person father = new Person();
        father.setName("Dad");
        father.setMaritalStatus(MaritalStatus.MARRIED);
        
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
        mother.setMaritalStatus(MaritalStatus.MARRIED);
        mother.setSpouse(father);

        father.setSpouse(mother);

        Person child1 = new Person();
        child1.setName("Jack");
        child1.setMaritalStatus(MaritalStatus.MARRIED);

        Person daughterInLaw = new Person();
        daughterInLaw.setName("Lisa");
        daughterInLaw.setSpouse(child1);
        daughterInLaw.setMaritalStatus(MaritalStatus.MARRIED);

        child1.setSpouse(daughterInLaw);

        Person child2 = new Person();
        child2.setName("Jill");
        child2.setMaritalStatus(MaritalStatus.SINGLE);

        Set<Person> children = new HashSet<Person>();
        children.add(child1);
        children.add(child2);

        father.setChildren(children);
        mother.setChildren(children);
        
        return father;
    }
}
