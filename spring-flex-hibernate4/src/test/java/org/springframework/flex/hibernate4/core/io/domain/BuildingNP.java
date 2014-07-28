package org.springframework.flex.hibernate4.core.io.domain;

import java.util.Set;

import javax.persistence.ElementCollection;
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

	@ElementCollection
	public Set<EmbeddedFloorNP> floors;
}
