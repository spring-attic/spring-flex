
package org.springframework.flex.core.io;

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
}
