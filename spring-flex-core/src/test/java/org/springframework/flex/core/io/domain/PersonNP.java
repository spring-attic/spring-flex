
package org.springframework.flex.core.io.domain;

import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PersonNP {

    public Integer id;

    public Integer version;

    public String name;

    public PersonNP spouse;

    public AddressNP address;

    public Set<AddressNP> previousAddresses;

    public Set<PersonNP> children;

}
