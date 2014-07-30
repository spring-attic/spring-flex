package org.springframework.flex.hibernate4.domain;


public enum MaritalStatus {

    SINGLE, MARRIED, DIVORCED;
    
    public String value() {
        return name();
    }
    
    public static MaritalStatus fromValue(String v) {
        return valueOf(v);
    }
}
