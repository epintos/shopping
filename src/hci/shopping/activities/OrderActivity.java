package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.model.api.OrderInfo;
import hci.shopping.model.impl.OrderInfoProviderImpl;
import hci.shopping.services.OrderService;
import hci.shopping.services.OrderUpdateService;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class OrderActivity extends ListActivity {
	private String TAG = getClass().getSimpleName();

	private final int HELP_ITEM_ID = 1;
	private final int LOG_OUT_ITEM_ID = 2;
	private final int SETTINGS_ITEM_ID = 3;
	private static final int HOME_ITEM_ID = 4;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private int order_id;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		order_id = new Integer(this.getIntent().getExtras()
				.getString("order_id"));

		setTitle(getString(R.string.app_name) + " | "
				+ getString(R.string.order_number) + " " + order_id);
		dialog = ProgressDialog.show(OrderActivity.this, "",
				getString(R.string.loading_please_wait), true);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				OrderService.class);
		intent.putExtra("command", OrderService.GET_ORDER_CMD);
		intent.putExtra("order_id", order_id);
		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				dialog.dismiss();
				setContentView(R.layout.order);
				if (resultCode == OrderService.STATUS_OK) {

					Log.d(TAG, "OK");

					@SuppressWarnings("unchecked")
					List<OrderInfo> list = (List<OrderInfo>) resultData
							.getSerializable("return");

					orderInfoList(new OrderInfoProviderImpl(list));

				} else if (resultCode == OrderService.STATUS_CONNECTION_ERROR) {
					Log.d(TAG, "Connection error.");
					Toast.makeText(getApplicationContext(),
							getString(R.string.connection_error),
							Toast.LENGTH_SHORT).show();
				} else {
					Log.d(TAG, "Unknown error.");
					Toast.makeText(getApplicationContext(),
							getString(R.string.connection_error),
							Toast.LENGTH_SHORT).show();
				}
			}

		});
		startService(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		dialog.dismiss();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FINISH) {
			setResult(FINISH);
			OrderActivity.this.finish();
		}
	}

	private void orderInfoList(OrderInfoProviderImpl orderInfoProvider) {
		ListAdapter adapter = new SimpleAdapter(this,
				orderInfoProvider.getOrderInfoAsMap(),
				R.layout.order_info_item, orderInfoProvider.getMapKeys(),
				new int[] { R.id.order_id, R.id.order_status,
						R.id.order_confirmed_date, R.id.order_total_price });

		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(OPTIONS_MENU_GROUP_ID, HOME_ITEM_ID, Menu.FIRST, R.string.home)
				.setIcon(R.drawable.home);
		menu.add(OPTIONS_MENU_GROUP_ID, SETTINGS_ITEM_ID, Menu.FIRST + 1,
				R.string.settings).setIcon(R.drawable.settings);
		menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 2,
				R.string.help).setIcon(R.drawable.help);
		menu.add(OPTIONS_MENU_GROUP_ID, LOG_OUT_ITEM_ID, Menu.FIRST + 3,
				R.string.log_out).setIcon(R.drawable.log_out);

		return true; // True for the menu to be visible
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case HOME_ITEM_ID:
			goToHome();
			return true;
		case LOG_OUT_ITEM_ID:
			showMain();
			return true;
		case HELP_ITEM_ID:
			showHelp();
			return true;
		case SETTINGS_ITEM_ID:
			showSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showSettings() {
		Intent intent = new Intent(OrderActivity.this, SettingsActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showCategories() {
		Intent intent = new Intent(OrderActivity.this, CategoriesActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToHome() {
		Intent intent = new Intent(OrderActivity.this, HomeActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showMain() {
		SharedPreferences settings = getSharedPreferences("user", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		Intent intentService = new Intent(Intent.ACTION_SYNC, null, this,
				OrderUpdateService.class);
		stopService(intentService);
		editor.putString("username", null);
		editor.putString("authentication_token", null);
		editor.commit();
		Intent intent = new Intent(OrderActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		OrderActivity.this.finish();
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_order))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
