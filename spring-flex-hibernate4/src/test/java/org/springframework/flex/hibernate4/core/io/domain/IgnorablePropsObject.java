package org.springframework.flex.hibernate4.core.io.domain;


import org.springframework.flex.core.io.AmfIgnore;
import org.springframework.flex.core.io.AmfIgnoreField;

public class IgnorablePropsObject {

    @AmfIgnoreField
    private String fooField = "unset";
    
    @AmfIgnoreField(onDeserialization=false)
    private String barField = "unset";
    
    @AmfIgnoreField(onSerialization=false)
    private String bazField = "unset";

    @AmfIgnore
    public String getFoo() {
        return fooField;
    }

    @AmfIgnore
    public void setFoo(String foo) {
        this.fooField = foo;
    }

    @AmfIgnore
    public String getBar() {
        return barField;
    }

    public void setBar(String bar) {
        this.barField = bar;
    }

    public String getBaz() {
        return bazField;
    }

    @AmfIgnore
    public void setBaz(String baz) {
        this.bazField = baz;
    }
}
