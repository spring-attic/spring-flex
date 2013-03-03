package org.springframework.flex.core.io.domain;

import java.util.Set;

import javax.persistence.*;

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
