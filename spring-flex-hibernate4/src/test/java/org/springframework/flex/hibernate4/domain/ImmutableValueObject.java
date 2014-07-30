package org.springframework.flex.hibernate4.domain;

import org.springframework.flex.core.io.AmfCreator;
import org.springframework.flex.core.io.AmfProperty;


public class ImmutableValueObject {

    final String foo;
    
    final Integer zoo;
    
    ImmutableValueObject voRef;
    
    Person personRef;
    
    @AmfCreator
    public ImmutableValueObject(@AmfProperty("foo") String foo, @AmfProperty("zoo") Integer zoo) {
        this.foo = foo;
        this.zoo = zoo;
    }
    
    public String getFoo() {
        return this.foo;
    }
    
    public Integer getZoo() {
        return this.zoo;
    }
    
    public Person getPersonRef() {
        return personRef;
    }

    public void setPersonRef(Person personRef) {
        this.personRef = personRef;
    }
    
    public ImmutableValueObject getVoRef() {
        return voRef;
    }

    public void setVoRef(ImmutableValueObject voRef) {
        this.voRef = voRef;
    }
}
