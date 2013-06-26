package hci.shopping.model.api;

import java.util.List;
import java.util.Map;

public interface OrderProvider {
	public List<Order> getOrders();

	public Map<String, Order> getOrdersAsMap();

	public List<String> getOrdersID();
}
