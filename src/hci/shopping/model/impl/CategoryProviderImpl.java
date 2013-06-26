package hci.shopping.model.impl;

import hci.shopping.model.api.Category;
import hci.shopping.model.api.CategoryProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryProviderImpl implements CategoryProvider {

	private List<Category> categories;

	public CategoryProviderImpl(List<Category> list) {
		this.categories = list;
	}

	@Override
	public List<Category> getCategories() {
		return categories;
	}

	@Override
	public Map<String, Category> getCategoriesAsMap() {
		HashMap<String, Category> map = new HashMap<String, Category>();
		for (Category category : categories) {
			map.put(category.getName(), category);
		}
		return map;
	}

	@Override
	public List<String> getCategoriesName() {
		List<String> list = new ArrayList<String>();
		for (Category category : categories) {
			list.add(category.getName());
		}
		return list;
	}
}
