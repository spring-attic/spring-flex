package org.springframework.flex.hibernate3.domain;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloorAttributesNP {

    //Hibernate complains if this one doesn't have getters and setters - thinking that's a Hibernate bug
    
    public Integer emergencyExits;

    
    public Integer getEmergencyExits() {
        return emergencyExits;
    }

    
    public void setEmergencyExits(Integer emergencyExits) {
        this.emergencyExits = emergencyExits;
    }
    
    

}
