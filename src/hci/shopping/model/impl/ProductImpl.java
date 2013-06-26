package hci.shopping.model.impl;

import hci.shopping.model.api.Product;
import android.graphics.drawable.Drawable;

public class ProductImpl implements Product {

	private String ID;
	private String name;
	private Drawable image;
	private String ranking;
	private String price;

	public ProductImpl(String ID, String name, Drawable image, String ranking,
			String price) {
		this.ID = ID;
		this.name = name;
		this.image = image;
		this.ranking = ranking;
		this.price = price;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Drawable getImage() {
		return image;
	}

	@Override
	public String getRanking() {
		return ranking;
	}

	@Override
	public String getPrice() {
		return price;
	}

}
