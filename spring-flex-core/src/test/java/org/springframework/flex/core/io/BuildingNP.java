package org.springframework.flex.core.io;

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
    Integer id;
	
	@Embedded
	EmbeddedAddressNP address;
	
	@org.hibernate.annotations.CollectionOfElements
    Set<EmbeddedFloorNP> floors;
}
