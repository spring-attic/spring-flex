package org.springframework.flex.core.io.domain;

import org.springframework.flex.core.io.AmfCreator;
import org.springframework.flex.core.io.AmfProperty;


public class ImmutableValueObject {

    final String foo;
    
    final Integer zoo;
    
    ImmutableValueObject voRef;
    
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
    
    public ImmutableValueObject getVoRef() {
        return voRef;
    }

    public void setVoRef(ImmutableValueObject voRef) {
        this.voRef = voRef;
    }
}
