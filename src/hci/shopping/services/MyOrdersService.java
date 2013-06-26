package hci.shopping.services;

import hci.shopping.R;
import hci.shopping.model.api.Order;
import hci.shopping.model.impl.OrderImpl;

import java.io.IOException;
import java.io.Serializable;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class MyOrdersService extends IntentService {
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int STATUS_OK = 0;

	public static final String GET_ORDERS_CMD = "GetOrders";
	private final String TAG = getClass().getSimpleName();

	public MyOrdersService() {
		super("MyOrdersService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final Bundle b = new Bundle();
		try {
			if (command.equals(GET_ORDERS_CMD)) {
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

		this.stopSelf();
	}

	private void getOrders(ResultReceiver receiver, Bundle b)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutSocket = 60000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		final DefaultHttpClient client = new DefaultHttpClient(httpParameters);
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
		b.putSerializable("return", (Serializable) fromXMLToOrders(xmlString));

		receiver.send(STATUS_OK, b);
	}

	private List<Order> fromXMLToOrders(String xmlString) throws IOException,
			ParserConfigurationException, SAXException {
		List<Order> orders = new ArrayList<Order>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);

		NodeList nodeList = doc.getElementsByTagName("order");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Element order = (Element) node;
			NodeList statusList = order.getElementsByTagName("status");
			String allStatus[] = { getString(R.string.created),
					getString(R.string.confirmed), getString(R.string.on_way),
					getString(R.string.delivered) };
			Node status = statusList.item(0);
			orders.add(new OrderImpl(order.getAttribute("id"),
					allStatus[Integer.parseInt(status.getFirstChild()
							.getNodeValue()) - 1]));
		}
		return orders;
	}
}
