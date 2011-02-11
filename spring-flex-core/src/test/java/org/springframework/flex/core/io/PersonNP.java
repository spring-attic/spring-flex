
package org.springframework.flex.core.io;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class PersonNP {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Version
    @Column(name = "version")
    Integer version;

    String name;

    @OneToOne(fetch = FetchType.LAZY)
    PersonNP spouse;

    @OneToOne
    AddressNP address;

    @OneToMany
    Set<AddressNP> previousAddresses;

    @ManyToMany
    Set<PersonNP> children;

}
