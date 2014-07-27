package org.springframework.flex.hibernate3.domain;


public enum MaritalStatus {

    SINGLE, MARRIED, DIVORCED;
    
    public String value() {
        return name();
    }
    
    public static MaritalStatus fromValue(String v) {
        return valueOf(v);
    }
}
