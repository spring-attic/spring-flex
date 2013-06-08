package org.springframework.flex.hibernate3.core.io.domain;


public class PrimitiveCompany {

    private int id;

    private int version;

    private String name;

	private Company company;
    
    public int getId() {
        return id;
    }

    
    public void setId(int id) {
        this.id = id;
    }

    
    public int getVersion() {
        return version;
    }

    
    public void setVersion(int version) {
        this.version = version;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
}
