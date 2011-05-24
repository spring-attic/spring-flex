package org.springframework.flex.core.io.domain;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloorNP {

    public Integer units;
    
    public EmbeddedFloorAttributesNP embeddedFloorAttributes;
}
