package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.model.api.Order;
import hci.shopping.model.impl.OrderProviderImpl;
import hci.shopping.services.MyOrdersService;
import hci.shopping.services.OrderUpdateService;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyOrdersActivity extends ListActivity {

	private String TAG = getClass().getSimpleName();

	private final int LOG_OUT_ITEM_ID = 1;
	private final int SETTINGS_ITEM_ID = 2;
	private static final int HOME_ITEM_ID = 3;
	private final int HELP_ITEM_ID = 5;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private OrderAdapter orderAdapter;
	private ArrayList<Order> my_orders = new ArrayList<Order>();
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.app_name) + " | "
				+ getString(R.string.my_orders));
		dialog = ProgressDialog.show(MyOrdersActivity.this, "",
				getString(R.string.loading_please_wait), true);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				MyOrdersService.class);
		intent.putExtra("command", MyOrdersService.GET_ORDERS_CMD);

		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				dialog.dismiss();
				setContentView(R.layout.my_orders);
				orderAdapter = new OrderAdapter(MyOrdersActivity.this,
						R.layout.my_orders_item, my_orders);
				setListAdapter(orderAdapter);
				if (resultCode == MyOrdersService.STATUS_OK) {

					Log.d(TAG, "OK");

					@SuppressWarnings("unchecked")
					List<Order> list = (List<Order>) resultData
							.getSerializable("return");

					orderList(new OrderProviderImpl(list));

				} else if (resultCode == MyOrdersService.STATUS_CONNECTION_ERROR) {
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
			MyOrdersActivity.this.finish();
		}
	}

	private void orderList(OrderProviderImpl orderProvider) {
		for (Order order : orderProvider.getOrders()) {
			my_orders.add(order);
		}
		orderAdapter.notifyDataSetChanged();
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setCacheColorHint(0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		Bundle bundle = new Bundle();
		bundle.putString("order_id", ((Order) o).getID() + "");
		Intent intent = new Intent(MyOrdersActivity.this, OrderActivity.class);
		intent.putExtras(bundle);
		startActivityForResult(intent, 0);
	}

	private class OrderAdapter extends ArrayAdapter<Order> {

		private ArrayList<Order> orders;

		public OrderAdapter(Context context, int textViewResourceId,
				ArrayList<Order> items) {
			super(context, textViewResourceId, items);
			this.orders = items;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.my_orders_item, null);
			}
			Order order = orders.get(position);
			if (order != null) {
				TextView order_id = (TextView) view.findViewById(R.id.order_id);
				TextView order_status = (TextView) view
						.findViewById(R.id.order_status);

				if (order_id != null) {
					order_id.setText(order.getID());
				}
				if (order_status != null) {
					order_status.setText(order.getStatus());
				}
			}
			return view;
		}
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
		case SETTINGS_ITEM_ID:
			showSettings();
			return true;
		case HELP_ITEM_ID:
			showHelp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showSettings() {
		Intent intent = new Intent(MyOrdersActivity.this,
				SettingsActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToHome() {
		Intent intent = new Intent(MyOrdersActivity.this, HomeActivity.class);
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
		Intent intent = new Intent(MyOrdersActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		MyOrdersActivity.this.finish();
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_orders))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
