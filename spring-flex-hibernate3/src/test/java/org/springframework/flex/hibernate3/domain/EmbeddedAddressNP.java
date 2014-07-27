
package org.springframework.flex.hibernate3.domain;

import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedAddressNP {

    public String street;

    public String city;

    public String state;

    public String zipcode;

    public Integer rooms;

    public Date moveInDate;

}
