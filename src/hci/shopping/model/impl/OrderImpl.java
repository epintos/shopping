package hci.shopping.model.impl;

import hci.shopping.model.api.Order;

public class OrderImpl implements Order {

	private String ID;
	private String address;
	private String status;
	private String createdDate;
	private String confirmedDate;
	private String shippedDate;
	private String deliveredDate;
	private String latitude;
	private String longitude;

	public void setConfirmedDate(String confirmedDate) {
		this.confirmedDate = confirmedDate;
	}

	public OrderImpl(String ID, String address, String status,
			String createdDate, String confirmedDate, String shippedDate,
			String deliveredDate, String latitude, String longitude) {
		this.ID = ID;
		this.address = address;
		this.status = status;
		this.createdDate = createdDate;
		this.confirmedDate = confirmedDate;
		this.shippedDate = shippedDate;
		this.deliveredDate = deliveredDate;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public OrderImpl(String ID, String status) {
		this.ID = ID;
		this.status = status;
	}

	public String getID() {
		return ID;
	}

	public String getAddress() {
		return address;
	}

	public String getStatus() {
		return status;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public String getConfirmedDate() {
		return confirmedDate;
	}

	public String getShippedDate() {
		return shippedDate;
	}

	public String getDeliveredDate() {
		return deliveredDate;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		result = prime * result
				+ ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result
				+ ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderImpl other = (OrderImpl) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}
	
	public void setOrderInfo(Order order){
		this.status=order.getStatus();
		this.latitude=order.getLatitude();
		this.longitude=order.getLongitude();
	}

}
