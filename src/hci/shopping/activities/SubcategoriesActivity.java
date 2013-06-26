package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.model.api.Category;
import hci.shopping.model.api.CategoryProvider;
import hci.shopping.model.impl.CategoryProviderImpl;
import hci.shopping.services.OrderUpdateService;
import hci.shopping.services.SubcategoriesService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SubcategoriesActivity extends ListActivity {
	private String TAG = getClass().getSimpleName();
	private final int MAIN_ITEM_ID = 1;
	private final int HOME_ITEM_ID = 2;
	private final int CATEGORY_ITEM_ID = 3;
	private final int LOG_OUT_ITEM_ID = 4;
	private final int SETTINGS_ITEM_ID = 5;
	private final int HELP_ITEM_ID = 6;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private int category_id;
	private String category_name;
	private Map<String, Category> subcategories = new HashMap<String, Category>();
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		category_id = new Integer(this.getIntent().getExtras()
				.getString("category_id"));
		category_name = new String(this.getIntent().getExtras()
				.getString("category_name"));
		
		setTitle(getString(R.string.categories) + " > " + category_name);

		dialog = ProgressDialog.show(SubcategoriesActivity.this, "",
				getString(R.string.loading_please_wait), true);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				SubcategoriesService.class);
		intent.putExtra("command", SubcategoriesService.GET_SUBCATEGORIES_CMD);
		intent.putExtra("category_id", category_id);
		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				dialog.dismiss();
				setContentView(R.layout.subcategories);
				if (resultCode == SubcategoriesService.STATUS_OK) {

					Log.d(TAG, "OK");

					@SuppressWarnings("unchecked")
					List<Category> list = (List<Category>) resultData
							.getSerializable("return");

					subcategoryList(new CategoryProviderImpl(list));

				} else if (resultCode == SubcategoriesService.STATUS_CONNECTION_ERROR) {
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
			SubcategoriesActivity.this.finish();
		}
	}

	private void subcategoryList(CategoryProvider categoryProvider) {
		subcategories = categoryProvider.getCategoriesAsMap();
		SubcategoriesActivity.this.setListAdapter(new ArrayAdapter<String>(
				SubcategoriesActivity.this, R.layout.subcategory_item,
				categoryProvider.getCategoriesName()));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setCacheColorHint(0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		String cat = o.toString();
		Bundle bundle = new Bundle();
		bundle.putString("subcategory_id", subcategories.get(cat).getID());
		bundle.putString("category_id", category_id + "");
		bundle.putString("bread_crumb", getString(R.string.categories) + " > "
				+ category_name + " > " + subcategories.get(cat).getName());
		Intent intent = new Intent(SubcategoriesActivity.this,
				ProductsActivity.class);
		intent.putExtras(bundle);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		SharedPreferences settings = getSharedPreferences("user", MODE_PRIVATE);
		String username = settings.getString("username", null);
		if (username != null) {
			menu.add(OPTIONS_MENU_GROUP_ID, HOME_ITEM_ID, Menu.FIRST,
					R.string.home).setIcon(R.drawable.home);
			menu.add(OPTIONS_MENU_GROUP_ID, SETTINGS_ITEM_ID, Menu.FIRST + 1,
					R.string.settings).setIcon(R.drawable.settings);
			menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 2,
					R.string.help).setIcon(R.drawable.help);
			menu.add(OPTIONS_MENU_GROUP_ID, LOG_OUT_ITEM_ID, Menu.FIRST + 3,
					R.string.log_out).setIcon(R.drawable.log_out);

		} else {
			menu.add(OPTIONS_MENU_GROUP_ID, MAIN_ITEM_ID, Menu.FIRST,
					R.string.home).setIcon(R.drawable.home);
			menu.add(OPTIONS_MENU_GROUP_ID, CATEGORY_ITEM_ID, Menu.FIRST + 1,
					R.string.categories).setIcon(R.drawable.categories);
			menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 3,
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
		case SETTINGS_ITEM_ID:
			showSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showSettings() {
		Intent intent = new Intent(SubcategoriesActivity.this,
				SettingsActivity.class);
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
		Intent intent = new Intent(SubcategoriesActivity.this,
				MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		SubcategoriesActivity.this.finish();
	}

	public void showCategories() {
		Intent intent = new Intent(SubcategoriesActivity.this,
				CategoriesActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToMain() {
		Intent intent = new Intent(SubcategoriesActivity.this,
				MainActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToHome() {
		Intent intent = new Intent(SubcategoriesActivity.this,
				HomeActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_subcategories))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
