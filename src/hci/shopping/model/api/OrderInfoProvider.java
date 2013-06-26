package hci.shopping.model.api;

import java.util.List;
import java.util.Map;

public interface OrderInfoProvider {
	public List<OrderInfo> getProducts();

	public List<? extends Map<String, ?>> getOrderInfoAsMap();

	public String[] getMapKeys();
}
