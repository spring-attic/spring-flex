
package org.springframework.flex.hibernate4.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AddressNP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    public String street;

    public String city;

    public String state;

    public String zipcode;

    public Integer rooms;

    public Date moveInDate;
}
