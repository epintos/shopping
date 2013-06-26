package hci.shopping.model.impl;

import hci.shopping.model.api.ProductInfo;
import hci.shopping.model.api.ProductInfoProvider;

public class ProductInfoProviderImpl implements ProductInfoProvider {
	private ProductInfo product;

	public ProductInfoProviderImpl(ProductInfo product) {
		this.product = product;
	}

	@Override
	public ProductInfo getProduct() {
		return product;
	}

}
