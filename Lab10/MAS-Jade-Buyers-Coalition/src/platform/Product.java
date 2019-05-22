package platform;

import java.io.Serializable;

public class Product implements Serializable, Comparable<Product> {
	
	private static final long serialVersionUID = -8638411182514528903L;
    
	String type;
	int value;
	int price;
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
	
	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}
	
	/**
	 * @param price the price to set
	 */
	public void setPrice(int price) {
		this.price = price;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + price;
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
	    result = prime * result + value;
	    return result;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    Product other = (Product) obj;
	    if (price != other.price)
		    return false;
	    if (type == null) {
		    if (other.type != null)
			    return false;
	    }
	    else if (!type.equals(other.type))
		    return false;
	    if (value != other.value)
		    return false;
	    return true;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
	    return "Product [type=" + type + ", value=" + value + ", price="
	            + price + "]";
    }

	@Override
    public int compareTo(Product other) {
	    // sorting in decreasing order of value
		return other.value - value;
    }
	
}
