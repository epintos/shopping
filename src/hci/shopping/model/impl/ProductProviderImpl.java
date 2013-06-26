package hci.shopping.model.impl;

import hci.shopping.model.api.Product;
import hci.shopping.model.api.ProductProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductProviderImpl implements ProductProvider {
	private ArrayList<Product> products;
	public static final String[] fields = { "name", "image", "ranking", "price" };

	public ProductProviderImpl(ArrayList<Product> list) {
		products = list;
	}

	@Override
	public ArrayList<Product> getProducts() {
		return products;
	}

	@Override
	public Map<String, Product> getProductsAsMap() {
		HashMap<String, Product> map = new HashMap<String, Product>();
		for (Product product : products) {
			map.put(product.getID(), product);
		}
		return map;
	}

	@Override
	public String[] getMapKeys() {
		return fields;
	}

}
