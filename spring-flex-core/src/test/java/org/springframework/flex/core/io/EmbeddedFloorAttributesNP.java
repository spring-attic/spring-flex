package org.springframework.flex.core.io;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloorAttributesNP {

    //Hibernate complains if this one doesn't have getters and setters - thinking that's a Hibernate bug
    
    Integer emergencyExits;

    
    public Integer getEmergencyExits() {
        return emergencyExits;
    }

    
    public void setEmergencyExits(Integer emergencyExits) {
        this.emergencyExits = emergencyExits;
    }
    
    

}
