package hci.shopping.activities;

import hci.shopping.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int HELP_ITEM_ID = 1;
	private static final int ABOUT_US_ITEM_ID = 2;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SharedPreferences settings = getSharedPreferences("user", MODE_PRIVATE);
		String username = settings.getString("username", null);
		if (username != null) {
			goToHome();
		}

		ImageButton categories = (ImageButton) findViewById(R.id.categories);
		categories.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showCategories();
			}

		});
		ImageView search = (ImageView) findViewById(R.id.support_search);
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.support_search), Toast.LENGTH_SHORT)
						.show();
			}

		});
		ImageButton log_in = (ImageButton) findViewById(R.id.log_in);
		log_in.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,
						LogInActivity.class);
				startActivityForResult(intent, 0);
			}

		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FINISH) {
			setResult(FINISH);
			MainActivity.this.finish();
		}
	}

	public void showCategories() {
		Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST, R.string.help)
				.setIcon(R.drawable.help);
		menu.add(OPTIONS_MENU_GROUP_ID, ABOUT_US_ITEM_ID, Menu.FIRST + 1,
				R.string.about_us).setIcon(R.drawable.about_us);

		return true; // True for the menu to be visible
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case HELP_ITEM_ID:
			showHelp();
			return true;
		case ABOUT_US_ITEM_ID:
			showAboutUs();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void goToHome() {
		Intent intent = new Intent(MainActivity.this, HomeActivity.class);
		startActivityForResult(intent, 0);
		setResult(FINISH);
		MainActivity.this.finish();
	}

	public void showSettings() {
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_main))
				.setPositiveButton(android.R.string.ok, null).show();
	}

	public void showAboutUs() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.about_us))
				.setMessage(getString(R.string.about_us_text))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
