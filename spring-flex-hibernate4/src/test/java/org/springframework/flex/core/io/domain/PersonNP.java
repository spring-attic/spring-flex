
package org.springframework.flex.core.io.domain;

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
    public Integer id;

    @Version
    @Column(name = "version")
    public Integer version;

    public String name;

    @OneToOne(fetch = FetchType.LAZY)
    public PersonNP spouse;

    @OneToOne
    public AddressNP address;

    @OneToMany
    public Set<AddressNP> previousAddresses;

    @ManyToMany
    public Set<PersonNP> children;

}
