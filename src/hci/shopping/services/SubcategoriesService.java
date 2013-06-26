package hci.shopping.services;

import hci.shopping.model.api.Category;
import hci.shopping.model.impl.CategoryImpl;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
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
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.Html;
import android.util.Log;

public class SubcategoriesService extends IntentService {
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int STATUS_OK = 0;

	public static final String GET_SUBCATEGORIES_CMD = "GetSubcategories";
	private final String TAG = getClass().getSimpleName();

	public SubcategoriesService() {
		super("SubcategoriesService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final int category_id = intent.getIntExtra("category_id", 1);
		final Bundle b = new Bundle();
		try {
			if (command.equals(GET_SUBCATEGORIES_CMD)) {
				getSubcategories(receiver, b, category_id);
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

	private void getSubcategories(ResultReceiver receiver, Bundle b,
			int category_id) throws ClientProtocolException, IOException,
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
		String url = "http://eiffel.itba.edu.ar/hci/service/Catalog.groovy?method=GetSubcategoryList&language_id="
				+ language_id + "&category_id=" + category_id;
		final HttpResponse response = client.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException(response.getStatusLine()
					.toString());
		}

		final String xmlString = EntityUtils.toString(response.getEntity());
		b.putSerializable("return",
				(Serializable) fromXMLToSubcategories(xmlString));

		receiver.send(STATUS_OK, b);
	}

	private List<Category> fromXMLToSubcategories(String xmlString)
			throws IOException, ParserConfigurationException, SAXException {
		List<Category> categories = new ArrayList<Category>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);
		doc.getDocumentElement().normalize();
		NodeList nodeList = doc.getElementsByTagName("subcategory");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Element category = (Element) node;
			NodeList nameList = category.getElementsByTagName("name");
			Node name = nameList.item(0);
			categories.add(new CategoryImpl(category.getAttribute("id"), Html.fromHtml(name
					.getFirstChild().getNodeValue()).toString()));
		}
		return categories;
	}
	
	
}
