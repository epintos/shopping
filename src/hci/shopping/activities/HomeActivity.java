package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.services.OrderUpdateService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeActivity extends Activity {

	private final int HELP_ITEM_ID = 1;
	private final int ABOUT_US_ID = 2;
	private final int SETTINGS_ITEM_ID = 3;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private String TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		SharedPreferences settings = getSharedPreferences("notification_delay",
				MODE_PRIVATE);
		String delay = settings.getString("delay", null);
		if (delay == null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("delay", "60000");
			editor.commit();
		}
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				OrderUpdateService.class);
		intent.putExtra("command", OrderUpdateService.GET_ORDER_CMD);

		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				if (resultCode == OrderUpdateService.STATUS_OK) {
					Log.d(TAG, "OK");
				} else if (resultCode == OrderUpdateService.STATUS_CONNECTION_ERROR) {
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
		ImageButton categories = (ImageButton) findViewById(R.id.categories);
		categories.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showCategories();
			}

		});

		ImageButton my_orders = (ImageButton) findViewById(R.id.my_orders);
		my_orders.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showMyOrders();
			}

		});

		ImageButton log_out = (ImageButton) findViewById(R.id.log_out);
		log_out.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SharedPreferences settings = getSharedPreferences("user",
						MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("username", null);
				editor.putString("authentication_token", null);
				editor.commit();
				showMain();
			}

		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FINISH) {
			Intent intentService = new Intent(Intent.ACTION_SYNC, null, this,
					OrderUpdateService.class);
			stopService(intentService);
			setResult(FINISH);
			HomeActivity.this.finish();
		}
	}

	public void showAboutUS() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.about_us))
				.setMessage(getString(R.string.about_us_text))
				.setPositiveButton(android.R.string.ok, null).show();
	}

	public void showCategories() {
		Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
		startActivityForResult(intent, 0);

	}

	public void showMyOrders() {
		Intent intent = new Intent(HomeActivity.this, MyOrdersActivity.class);
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
		Intent intent = new Intent(HomeActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		HomeActivity.this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(OPTIONS_MENU_GROUP_ID, SETTINGS_ITEM_ID, Menu.FIRST,
				R.string.settings).setIcon(R.drawable.settings);
		menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 1,
				R.string.help).setIcon(R.drawable.help);
		menu.add(OPTIONS_MENU_GROUP_ID, ABOUT_US_ID, Menu.FIRST + 2,
				R.string.about_us).setIcon(R.drawable.about_us);

		return true; // True for the menu to be visible
	}

	public void showSettings() {
		Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case HELP_ITEM_ID:
			showHelp();
			return true;
		case ABOUT_US_ID:
			showAboutUs();
			return true;
		case SETTINGS_ITEM_ID:
			showSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showAboutUs() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.about_us))
				.setMessage(getString(R.string.about_us_text))
				.setPositiveButton(android.R.string.ok, null).show();
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_home))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
