package org.springframework.flex.hibernate4.core.io.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class ContactInfoNP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;
    
    @Version
    @Column(name = "version")
    public int version;
    
    public String phone;
    
    public String email;

}
