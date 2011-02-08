
package org.springframework.flex.core.io;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AddressNP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    String street;

    String city;

    String state;

    String zipcode;

    Integer rooms;

    Date moveInDate;
}
