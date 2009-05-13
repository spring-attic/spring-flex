package flex.spring.samples.marketfeed;

import java.util.Date;
import java.io.Serializable;

public class Stock implements Serializable {

    private static final long serialVersionUID = -1763421100056755200L;
    
    protected String symbol;
	protected String name;
	protected double low;
	protected double high;
	protected double open;
	protected double last;
	protected double change;
	protected Date date;

	public double getChange() {
		return change;
	}
	public void setChange(double change) {
		this.change = change;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLast() {
		return last;
	}
	public void setLast(double last) {
		this.last = last;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
