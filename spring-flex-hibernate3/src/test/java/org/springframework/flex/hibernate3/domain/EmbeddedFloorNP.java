package org.springframework.flex.hibernate3.domain;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloorNP {

    public Integer units;

    public EmbeddedFloorAttributesNP embeddedFloorAttributes;
}
