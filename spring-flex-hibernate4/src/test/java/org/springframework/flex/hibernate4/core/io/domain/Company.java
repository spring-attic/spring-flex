package org.springframework.flex.hibernate4.core.io.domain;



public class Company {

    private Integer id;

    private Integer version;

    private String name;

	private PrimitiveCompany primitive;

    
    public Integer getId() {
        return id;
    }

    
    public void setId(Integer id) {
        this.id = id;
    }

    
    public Integer getVersion() {
        return version;
    }

    
    public void setVersion(Integer version) {
        this.version = version;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

	public PrimitiveCompany getPrimitive() {
		return primitive;
	}

	public void setPrimitive(PrimitiveCompany primitive) {
		this.primitive = primitive;
	}
}
