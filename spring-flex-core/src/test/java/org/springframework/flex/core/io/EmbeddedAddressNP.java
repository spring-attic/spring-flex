
package org.springframework.flex.core.io;

import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedAddressNP {

    String street;

    String city;

    String state;

    String zipcode;

    Integer rooms;

    Date moveInDate;

}
