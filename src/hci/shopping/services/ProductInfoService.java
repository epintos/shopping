package hci.shopping.services;

import hci.shopping.model.api.ProductInfo;
import hci.shopping.model.impl.ProductInfoImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class ProductInfoService extends IntentService {
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int STATUS_OK = 0;
	private String categoryId = "1";
	public static final String GET_PRODUC_INFO_CMD = "GetProductInfo";
	private final String TAG = getClass().getSimpleName();

	public ProductInfoService() {
		super("ProductInfoService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final int product_id = intent.getIntExtra("product_id", 1);
		final Bundle b = new Bundle();
		try {
			if (command.equals(GET_PRODUC_INFO_CMD)) {
				getProductInfo(receiver, b, product_id);
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

	private void getProductInfo(ResultReceiver receiver, Bundle b,
			int product_id) throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutSocket = 60000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		final DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		String url = "http://eiffel.itba.edu.ar/hci/service/Catalog.groovy?method=GetProduct&product_id="
				+ product_id;
		final HttpResponse response = client.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException(response.getStatusLine()
					.toString());
		}

		final String xmlString = EntityUtils.toString(response.getEntity());
		b.putSerializable("return",
				(Serializable) fromXMLToProductInfo(xmlString));
		b.putSerializable("category_id", categoryId);
		receiver.send(STATUS_OK, b);
	}

	private List<ProductInfo> fromXMLToProductInfo(String xmlString)
			throws IOException, ParserConfigurationException, SAXException {
		List<ProductInfo> productInfo = new ArrayList<ProductInfo>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);

		NodeList nodeList = doc.getElementsByTagName("product");
		Node node = nodeList.item(0);
		Element product = (Element) node;
		String category_id = product.getElementsByTagName("category_id")
				.item(0).getFirstChild().getNodeValue();
		Node name = product.getElementsByTagName("name").item(0);
		Node sales_rank = product.getElementsByTagName("sales_rank").item(0);
		Node price = product.getElementsByTagName("price").item(0);
		String image_url = product.getElementsByTagName("image_url").item(0)
				.getFirstChild().getNodeValue();
		Drawable image = downloadFile(image_url);
		categoryId = category_id;
		if (category_id.equals("1")) {
			Node actors = product.getElementsByTagName("actors").item(0);
			Node format = product.getElementsByTagName("format").item(0);
			Node subtitles = product.getElementsByTagName("subtitles").item(0);
			Node run_time = product.getElementsByTagName("run_time").item(0);
			productInfo.add(new ProductInfoImpl(product.getAttribute("id"),
					name.getFirstChild().getNodeValue(), image, sales_rank
							.getFirstChild().getNodeValue(), price
							.getFirstChild().getNodeValue(), actors
							.getFirstChild().getNodeValue(), format
							.getFirstChild().getNodeValue(), subtitles
							.getFirstChild().getNodeValue(), run_time
							.getFirstChild().getNodeValue()));
		} else {
			Node authors = product.getElementsByTagName("authors").item(0);
			Node publisher = product.getElementsByTagName("publisher").item(0);
			Node published_date = product
					.getElementsByTagName("published_date").item(0);
			Node ISBN_10 = product.getElementsByTagName("ISBN_10").item(0);
			Node language = product.getElementsByTagName("language").item(0);
			productInfo.add(new ProductInfoImpl(product.getAttribute("id"),
					name.getFirstChild().getNodeValue(), image, sales_rank
							.getFirstChild().getNodeValue(), price
							.getFirstChild().getNodeValue(), authors
							.getFirstChild().getNodeValue(), publisher
							.getFirstChild().getNodeValue(), published_date
							.getFirstChild().getNodeValue(), ISBN_10
							.getFirstChild().getNodeValue(), language
							.getFirstChild().getNodeValue()));
		}
		return productInfo;
	}

	private Drawable downloadFile(String url) {
		try {
			URL imageURL = new URL(url);
			InputStream is = (InputStream) imageURL.getContent();
			return Drawable.createFromStream(is, "image");
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
