package org.springframework.flex.hibernate3.domain;

import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BuildingNP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;

    @Embedded
    public EmbeddedAddressNP address;

    @SuppressWarnings("deprecation")
    @org.hibernate.annotations.CollectionOfElements
    public Set<EmbeddedFloorNP> floors;
}
