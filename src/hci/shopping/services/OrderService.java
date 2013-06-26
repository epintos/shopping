package hci.shopping.services;

import hci.shopping.R;
import hci.shopping.model.api.OrderInfo;
import hci.shopping.model.impl.OrderInfoImpl;

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

public class OrderService extends IntentService {
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int STATUS_OK = 0;

	public static final String GET_ORDER_CMD = "GetOrder";
	private final String TAG = getClass().getSimpleName();

	public OrderService() {
		super("OrderService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final int order_id = intent.getIntExtra("order_id", 1);
		final Bundle b = new Bundle();
		try {
			if (command.equals(GET_ORDER_CMD)) {
				getOrders(receiver, b, order_id);
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

	private void getOrders(ResultReceiver receiver, Bundle b, int order_id)
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
		String url = "http://eiffel.itba.edu.ar/hci/service/Order.groovy?method=GetOrder&username="
				+ username
				+ "&authentication_token="
				+ authentication_token
				+ "&order_id=" + order_id;
		final HttpResponse response = client.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException(response.getStatusLine()
					.toString());
		}

		final String xmlString = EntityUtils.toString(response.getEntity());
		b.putSerializable("return",
				(Serializable) fromXMLToOrderInfo(xmlString));

		receiver.send(STATUS_OK, b);
	}

	private List<OrderInfo> fromXMLToOrderInfo(String xmlString)
			throws IOException, ParserConfigurationException, SAXException {
		List<OrderInfo> orderInfo = new ArrayList<OrderInfo>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);

		NodeList nodeList = doc.getElementsByTagName("order");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Element order = (Element) node;
			Node status = order.getElementsByTagName("status").item(0);
			String confirmed_date = getString(R.string.not_confirmed);
			Node confirmed_dateNode = order
					.getElementsByTagName("confirmed_date").item(0)
					.getFirstChild();
			if (confirmed_dateNode != null) {
				confirmed_date = confirmed_dateNode.getNodeValue();
			}
			String allStatus[] = { getString(R.string.created),
					getString(R.string.confirmed), getString(R.string.on_way),
					getString(R.string.delivered) };
			NodeList item = doc.getElementsByTagName("item");
			double acum = 0;
			for (int j = 0; j < item.getLength(); j++) {
				double count = Double.parseDouble(order
						.getElementsByTagName("count").item(0).getFirstChild()
						.getNodeValue());
				double price = Double.parseDouble(order
						.getElementsByTagName("price").item(0).getFirstChild()
						.getNodeValue());
				acum += price * count;
			}
			Double totalPrice = acum;
			orderInfo.add(new OrderInfoImpl(order.getAttribute("id"),
					allStatus[Integer.parseInt(status.getFirstChild()
							.getNodeValue()) - 1], confirmed_date, "$"
							+ totalPrice.toString()));
		}
		return orderInfo;
	}
}
