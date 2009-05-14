package flex.spring.samples.product;
import java.io.Serializable;

public class Product implements Serializable {

    static final long serialVersionUID = 103844514947365244L;
    
    private int productId;
    private String name;
    private String description;
    private String image;
    private String category;
    private double price;
    private int qty;
    
    public Product() {
    	
    }
    
    public Product(int productId, String name, String description, String image, String category, double price, int qty) {
		this.productId = productId;
		this.name = name;
		this.description = description;
		this.image = image;
		this.category = category;
		this.price = price;
		this.qty = qty;
	}

    public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}

}