package flex.spring.samples.industry;

import java.io.Serializable;

public class Industry implements Serializable {

    static final long serialVersionUID = 103844514947365244L;
    
    private int id;
    private String name;
    
    public Industry() {
    	
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}