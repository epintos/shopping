package hci.shopping.model.impl;

import hci.shopping.model.api.OrderInfo;
import hci.shopping.model.api.OrderInfoProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderInfoProviderImpl implements OrderInfoProvider {
	private List<OrderInfo> orderInfo;
	public static final String[] fields = { "id", "status", "confirmed_date",
			"total_price" };

	public OrderInfoProviderImpl(List<OrderInfo> list) {
		orderInfo = list;
	}

	@Override
	public List<OrderInfo> getProducts() {
		return orderInfo;
	}

	@Override
	public List<? extends Map<String, ?>> getOrderInfoAsMap() {
		List<Map<String, String>> transformedProducts = new ArrayList<Map<String, String>>();
		for (OrderInfo order : orderInfo) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(fields[0], order.getID());
			map.put(fields[1], order.getStatus());
			map.put(fields[2], order.getConfirmedDate());
			map.put(fields[3], order.getTotalPrice());
			transformedProducts.add(map);
		}
		return transformedProducts;
	}

	@Override
	public String[] getMapKeys() {
		return fields;
	}

}
