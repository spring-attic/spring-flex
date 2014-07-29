package org.springframework.flex.hibernate4.core.io.domain;

import javax.persistence.Embeddable;

import org.springframework.flex.hibernate4.core.io.domain.EmbeddedFloorAttributesNP;

@Embeddable
public class EmbeddedFloorNP {

    public Integer units;

    public EmbeddedFloorAttributesNP embeddedFloorAttributes;
}
