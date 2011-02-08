package org.springframework.flex.core.io;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BuildingNP {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
	
	@Embedded
	private EmbeddedAddress address;

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
	
}
