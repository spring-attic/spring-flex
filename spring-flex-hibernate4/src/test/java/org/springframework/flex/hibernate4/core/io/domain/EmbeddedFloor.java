package org.springframework.flex.hibernate4.core.io.domain;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloor {

    private Integer units;
    
    private EmbeddedFloorAttributes embeddedFloorAttributes;
    
    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public void setEmbeddedFloorAttributes(EmbeddedFloorAttributes embeddedFloorAttributes) {
        this.embeddedFloorAttributes = embeddedFloorAttributes;
    }

    public EmbeddedFloorAttributes getEmbeddedFloorAttributes() {
        return embeddedFloorAttributes;
    }
   
}
