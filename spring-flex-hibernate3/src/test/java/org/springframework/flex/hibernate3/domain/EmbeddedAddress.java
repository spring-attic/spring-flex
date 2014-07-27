
package org.springframework.flex.hibernate3.domain;

import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedAddress {

    private String street;

    private String city;

    private String state;

    private String zipcode;

    private Integer rooms;

    private Date moveInDate;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipcode() {
        return zipcode;
    }
    
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public Integer getRooms() {
        return rooms;
    }

    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }

    public Date getMoveInDate() {
        return moveInDate;
    }

    public void setMoveInDate(Date moveInDate) {
        this.moveInDate = moveInDate;
    }    
}
