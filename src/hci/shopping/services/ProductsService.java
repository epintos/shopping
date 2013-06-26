package hci.shopping.services;

import hci.shopping.model.api.Product;
import hci.shopping.model.impl.ProductImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

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

public class ProductsService extends IntentService {
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int STATUS_OK = 0;
	private String resultsSize = "";
	public static final String GET_PRODUCTS_CMD = "GetProducts";
	private final String TAG = getClass().getSimpleName();

	public ProductsService() {
		super("ProductsService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final int category_id = intent.getIntExtra("category_id", 1);
		final int subcategory_id = intent.getIntExtra("subcategory_id", 1);
		Bundle b = new Bundle();
		try {
			if (command.equals(GET_PRODUCTS_CMD)) {
				getProducts(receiver, b, category_id, subcategory_id);
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

	private void getProducts(ResultReceiver receiver, Bundle b,
			int category_id, int subcategory_id)
			throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutSocket = 60000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		final DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		Locale locale = Locale.getDefault();
		String language = locale.getLanguage();
		int language_id;
		if (language.equals("es"))
			language_id = 2;
		else
			language_id = 1;
		String url = "http://eiffel.itba.edu.ar/hci/service/Catalog.groovy?method=GetProductListBySubcategory&language_id="
				+ language_id
				+ "&category_id="
				+ category_id
				+ "&subcategory_id=" + subcategory_id;
		HttpResponse response = client.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException(response.getStatusLine()
					.toString());
		}

		final String xmlString = EntityUtils.toString(response.getEntity());
		b.putSerializable("return", (Serializable) fromXMLToProducts(xmlString));
		b.putSerializable("results_size", (Serializable) resultsSize);
		receiver.send(STATUS_OK, b);
	}

	private ArrayList<Product> fromXMLToProducts(String xmlString)
			throws IOException, ParserConfigurationException, SAXException {
		ArrayList<Product> products = new ArrayList<Product>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);
		NodeList productsNodeList = doc.getElementsByTagName("products");
		Node productsNode = productsNodeList.item(0);
		Element productsElement = (Element) productsNode;
		resultsSize = productsElement.getAttribute("size");
		NodeList nodeList = doc.getElementsByTagName("product");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Element product = (Element) node;
			Node name = product.getElementsByTagName("name").item(0);
			Node sales_rank = product.getElementsByTagName("sales_rank")
					.item(0);
			Node price = product.getElementsByTagName("price").item(0);
			String image_url = product.getElementsByTagName("image_url")
					.item(0).getFirstChild().getNodeValue();
			Drawable image = downloadFile(image_url);
			products.add(new ProductImpl(product.getAttribute("id"), name
					.getFirstChild().getNodeValue(), image, sales_rank
					.getFirstChild().getNodeValue(), price.getFirstChild()
					.getNodeValue()));
		}
		return products;
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
