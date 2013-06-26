package hci.shopping.services;

import hci.shopping.R;
import hci.shopping.activities.MyOrdersActivity;
import hci.shopping.model.api.Order;
import hci.shopping.model.impl.OrderImpl;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class OrderUpdateService extends IntentService {
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int STATUS_OK = 0;
	private static final int ORDER_UPDATE_ID = 1;
	public static final String GET_ORDER_CMD = "GetOrder";
	private final String TAG = getClass().getSimpleName();
	private List<Order> orders = new ArrayList<Order>();
	private boolean firstTime = true;
	private boolean finish = false;

	public OrderUpdateService() {
		super("OrderUpdateService");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		finish = true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final Bundle b = new Bundle();
		while (!finish) {

			try {
				if (command.equals(GET_ORDER_CMD)) {
					getOrders(receiver, b);
				}
			} catch (SocketTimeoutException e) {
				Log.e(TAG, e.getMessage());
				receiver.send(STATUS_CONNECTION_ERROR, b);
			} catch (ClientProtocolException e) {
				Log.e(TAG, e.getMessage());
				receiver.send(STATUS_ERROR, b);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage());
				receiver.send(STATUS_ILLEGAL_ARGUMENT, b);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				receiver.send(STATUS_ERROR, b);
			} catch (ParserConfigurationException e) {
				Log.e(TAG, e.getMessage());
				receiver.send(STATUS_PARSER_ERROR, b);
			} catch (SAXException e) {
				Log.e(TAG, e.getMessage());
				receiver.send(STATUS_PARSER_ERROR, b);
			}
			SharedPreferences settings = getSharedPreferences(
					"notification_delay", MODE_PRIVATE);
			String delay = settings.getString("delay", "60000");
			int time = Integer.parseInt(delay);
			try {
				Thread.sleep(time);
			} catch (Exception e) {
				Log.d(TAG, "Order update service error");
			}
		}
		receiver.send(STATUS_OK, b);
		this.stopSelf();

	}

	private void getOrders(ResultReceiver receiver, Bundle b)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		final DefaultHttpClient client = new DefaultHttpClient();
		SharedPreferences settings = getSharedPreferences("user", MODE_PRIVATE);
		String username = settings.getString("username", "");
		String authentication_token = settings.getString(
				"authentication_token", "");
		String url = "http://eiffel.itba.edu.ar/hci/service/Order.groovy?method=GetOrderList&username="
				+ username + "&authentication_token=" + authentication_token;
		final HttpResponse response = client.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException(response.getStatusLine()
					.toString());
		}

		final String xmlString = EntityUtils.toString(response.getEntity());
		fromXMLToOrderInfo(xmlString);

	}

	private void fromXMLToOrderInfo(String xmlString) throws IOException,
			ParserConfigurationException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);
		NodeList nodeList = doc.getElementsByTagName("order");
		if (firstTime) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				Element order = (Element) node;
				Node status = order.getElementsByTagName("status").item(0);
				orders.add(new OrderImpl(order.getAttribute("id"), status
						.getFirstChild().getNodeValue()));
			}
			firstTime = false;
		} else {
			List<Order> newRequest = new ArrayList<Order>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				Element order = (Element) node;
				Node status = order.getElementsByTagName("status").item(0);
				newRequest.add(new OrderImpl(order.getAttribute("id"), status
						.getFirstChild().getNodeValue()));
			}
			int oldSize = orders.size();
			int newSize = newRequest.size();
			for (int i = 0; i < oldSize && newSize == oldSize; i++) {
				Order oldOrder = orders.get(i);
				Order newOrder = newRequest.get(i);
				if (!oldOrder.equals(newOrder)) {
					oldOrder.setOrderInfo(newOrder);
					notificateOrderUpdate(newOrder);
					Log.d(TAG, "An order status changed");
				}
			}
		}
	}

	private void notificateOrderUpdate(Order order) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText = getString(R.string.order_update);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = getString(R.string.order) + " " + order.getID()
				+ " " + getString(R.string.changed);
		String allStatus[] = { getString(R.string.created),
				getString(R.string.confirmed), getString(R.string.on_way),
				getString(R.string.delivered) };
		CharSequence contentText = getString(R.string.order_number) + " "
				+ order.getID() + " " + getString(R.string.status) + " "
				+ allStatus[Integer.parseInt(order.getStatus()) - 1];
		Intent notificationIntent = new Intent(this, MyOrdersActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		mNotificationManager.notify(ORDER_UPDATE_ID, notification);
	}

}
