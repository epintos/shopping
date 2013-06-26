package hci.shopping.model.impl;

import hci.shopping.model.api.Category;

public class CategoryImpl implements Category {

	private String ID;
	private String name;

	public CategoryImpl(String ID, String name) {
		this.ID = ID;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getID() {
		return ID;
	}

}
