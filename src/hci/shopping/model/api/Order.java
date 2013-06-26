package hci.shopping.model.api;

public interface Order {
	public String getID();

	public String getAddress();

	public String getStatus();

	public String getCreatedDate();

	public String getConfirmedDate();

	public String getShippedDate();

	public String getDeliveredDate();

	public String getLatitude();

	public String getLongitude();

	public void setOrderInfo(Order order);
}
