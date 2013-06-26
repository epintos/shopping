package hci.shopping.model.api;

import java.util.List;
import java.util.Map;

public interface ProductProvider {
	public List<Product> getProducts();

	public Map<String,Product> getProductsAsMap();

	public String[] getMapKeys();
	
}
