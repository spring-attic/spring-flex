package org.springframework.flex.hibernate4.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Version
    @Column(name = "version")
    private int version;
    
    private String phone;
    
    private String email;

    
    public int getId() {
        return id;
    }

    
    public void setId(int id) {
        this.id = id;
    }

    
    public void setVersion(int version) {
        this.version = version;
    }


    public int getVersion() {
        return version;
    }


    public String getPhone() {
        return phone;
    }

    
    public void setPhone(String phone) {
        this.phone = phone;
    }

    
    public String getEmail() {
        return email;
    }

    
    public void setEmail(String email) {
        this.email = email;
    }
}
