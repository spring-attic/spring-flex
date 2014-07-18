package org.springframework.flex.hibernate4.core.io.domain;

import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Building {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
	
	@Embedded
	private EmbeddedAddress address;
	
	@ElementCollection
	private Set<EmbeddedFloor> floors;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public EmbeddedAddress getAddress() {
		return address;
	}

	public void setAddress(EmbeddedAddress address) {
		this.address = address;
	}

    public void setFloors(Set<EmbeddedFloor> floors) {
        this.floors = floors;
    }

    public Set<EmbeddedFloor> getFloors() {
        return floors;
    }
	
}
