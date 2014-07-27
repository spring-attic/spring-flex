package org.springframework.flex.hibernate3.core.io.domain;

import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedFloorNP {

    public Integer units;

    public EmbeddedFloorAttributesNP embeddedFloorAttributes;
}
