package hci.shopping.model.impl;

import hci.shopping.model.api.Order;
import hci.shopping.model.api.OrderProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderProviderImpl implements OrderProvider {
	private List<Order> orders;
	public static final String[] fields = { "id", "status" };

	public OrderProviderImpl(List<Order> list) {
		orders = list;
	}

	@Override
	public List<Order> getOrders() {
		return orders;
	}

	@Override
	public Map<String, Order> getOrdersAsMap() {
		HashMap<String, Order> map = new HashMap<String, Order>();
		for (Order order : orders) {
			map.put(order.getID(), order);
		}
		return map;
	}

	@Override
	public List<String> getOrdersID() {
		// TODO Auto-generated method stub
		return null;
	}


}
