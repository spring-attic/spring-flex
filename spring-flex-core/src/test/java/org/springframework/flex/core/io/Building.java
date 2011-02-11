package org.springframework.flex.core.io;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Building {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
	
	@Embedded
	EmbeddedAddressNP address;
	
}
