package hci.shopping.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;

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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class LogInService extends IntentService {
	public static final int USER_PASS_OK = 0;
	public static final int STATUS_CONNECTION_ERROR = -1;
	public static final int STATUS_ERROR = -2;
	public static final int STATUS_PARSER_ERROR = -4;
	public static final int STATUS_ILLEGAL_ARGUMENT = -3;
	public static final int USER_PASS_ERROR = -5;

	public static final String GET_LOGIN_CMD = "LogIn";
	private final String TAG = getClass().getSimpleName();

	public LogInService() {
		super("LogInService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		final String command = intent.getStringExtra("command");
		final String username = intent.getStringExtra("username");
		final String password = intent.getStringExtra("password");
		final Bundle b = new Bundle();
		try {
			if (command.equals(GET_LOGIN_CMD)) {
				getUser(receiver, b, username, password);
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

	private void getUser(ResultReceiver receiver, Bundle b, String username,
			String password) throws ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutSocket = 60000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		final DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		String url = "http://eiffel.itba.edu.ar/hci/service/Security.groovy?method=SignIn&username="
				+ username + "&password=" + password;
		final HttpResponse response = client.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException(response.getStatusLine()
					.toString());
		}

		final String xmlString = EntityUtils.toString(response.getEntity());
		if (fromXMLToUser(xmlString) == false)
			receiver.send(USER_PASS_ERROR, b);
		else
			receiver.send(USER_PASS_OK, b);
	}

	private boolean fromXMLToUser(String xmlString) throws IOException,
			ParserConfigurationException, SAXException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(inStream);

		String status = ((Element) doc.getElementsByTagName("response").item(0))
				.getAttribute("status");
		if (status.equals("fail"))
			return false;
		String authentication_token = doc.getElementsByTagName("token").item(0)
				.getFirstChild().getNodeValue();
		String username = ((Element) doc.getElementsByTagName("user").item(0))
				.getAttribute("username");
		SharedPreferences settings = getSharedPreferences("user", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("username", username);
		editor.putString("authentication_token", authentication_token);
		editor.commit();
		return true;
	}

}
