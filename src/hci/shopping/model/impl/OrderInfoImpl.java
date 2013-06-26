package hci.shopping.model.impl;

import hci.shopping.model.api.OrderInfo;

public class OrderInfoImpl extends OrderImpl implements OrderInfo {

	private String totalPrice;

	public OrderInfoImpl(String ID, String status) {
		super(ID, status);
	}

	public OrderInfoImpl(String ID, String status, String confirmed_date,
			String totalPrice) {
		super(ID, status);
		super.setConfirmedDate(confirmed_date);
		this.totalPrice = totalPrice;
	}

	public String getTotalPrice() {
		return totalPrice;
	}

}
