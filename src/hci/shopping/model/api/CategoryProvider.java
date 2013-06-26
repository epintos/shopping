package hci.shopping.model.api;

import java.util.List;
import java.util.Map;

public interface CategoryProvider {
	public List<Category> getCategories();

	public Map<String, Category> getCategoriesAsMap();

	public List<String> getCategoriesName();

}
