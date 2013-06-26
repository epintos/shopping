package hci.shopping.model.api;

import android.graphics.drawable.Drawable;


public interface Product {

	public String getID();

	public String getName();

	public Drawable getImage();

	public String getRanking();

	public String getPrice();
}
