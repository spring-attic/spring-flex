package org.springframework.flex.core.io;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloor {

    private Integer units;
    
    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }
   
}
