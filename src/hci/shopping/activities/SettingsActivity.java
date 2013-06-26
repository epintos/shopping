package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.services.OrderUpdateService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private final int MAIN_ITEM_ID = 1;
	private final int HOME_ITEM_ID = 2;
	private final int CATEGORY_ITEM_ID = 3;
	private final int LOG_OUT_ITEM_ID = 4;
	private final int HELP_ITEM_ID = 5;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		setTitle(getString(R.string.app_name) + " | "
				+ getString(R.string.settings));
		TextView change_delay = (TextView) findViewById(R.id.change_delay);
		change_delay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final CharSequence[] delays = {
						"1 " + getString(R.string.minute),
						"3 " + getString(R.string.minutes),
						"6 " + getString(R.string.minutes) };
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SettingsActivity.this);
				builder.setTitle(getString(R.string.delay_time));
				builder.setItems(delays, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface,
							int item) {

						SharedPreferences settings = getSharedPreferences(
								"notification_delay", 0);
						Editor edit = settings.edit();
						switch (item) {
						case 0:
							edit.putString("delay", "60000");
							break;
						case 1:
							edit.putString("delay", "180000");
							break;
						case 2:
							edit.putString("delay", "360000");
							break;
						default:
							edit.putString("delay", "60000");
							break;
						}

						edit.commit();

						Toast.makeText(getApplicationContext(),
								getString(R.string.settings_update),
								Toast.LENGTH_SHORT).show();
						return;
					}
				});

				builder.create().show();
			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FINISH) {
			setResult(FINISH);
			SettingsActivity.this.finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		SharedPreferences settings = getSharedPreferences("user", MODE_PRIVATE);
		String username = settings.getString("username", null);
		if (username != null) {
			menu.add(OPTIONS_MENU_GROUP_ID, HOME_ITEM_ID, Menu.FIRST,
					R.string.home).setIcon(R.drawable.home);
			menu.add(OPTIONS_MENU_GROUP_ID, CATEGORY_ITEM_ID, Menu.FIRST + 1,
					R.string.categories).setIcon(R.drawable.categories);
			menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 2,
					R.string.help).setIcon(R.drawable.help);
			menu.add(OPTIONS_MENU_GROUP_ID, LOG_OUT_ITEM_ID, Menu.FIRST + 3,
					R.string.log_out).setIcon(R.drawable.log_out);
		} else {
			menu.add(OPTIONS_MENU_GROUP_ID, MAIN_ITEM_ID, Menu.FIRST,
					R.string.home).setIcon(R.drawable.home);
			menu.add(OPTIONS_MENU_GROUP_ID, CATEGORY_ITEM_ID, Menu.FIRST + 1,
					R.string.categories).setIcon(R.drawable.categories);
			menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 2,
					R.string.help).setIcon(R.drawable.help);
		}

		return true; // True for the menu to be visible
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_ITEM_ID:
			goToMain();
			return true;
		case HOME_ITEM_ID:
			goToHome();
			return true;
		case CATEGORY_ITEM_ID:
			showCategories();
			return true;
		case LOG_OUT_ITEM_ID:
			showMain();
			return true;
		case HELP_ITEM_ID:
			showHelp();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		SettingsActivity.this.finish();
	}

	public void showCategories() {
		Intent intent = new Intent(SettingsActivity.this,
				CategoriesActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToMain() {
		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToHome() {
		Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_settings))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
