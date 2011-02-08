package org.springframework.flex.core.io;

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
    int id;
    
    @Version
    @Column(name = "version")
    int version;
    
    String phone;
    
    String email;

}
