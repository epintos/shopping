package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.services.LogInService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LogInActivity extends Activity {
	private String TAG = getClass().getSimpleName();
	private final int MAIN_ITEM_ID = 1;
	private final int CATEGORY_ITEM_ID = 2;
	private final int HELP_ITEM_ID = 3;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in);
		setTitle(getString(R.string.app_name) + " | "
				+ getString(R.string.log_in));
		Button submitButton = (Button) findViewById(R.id.submit_log_in);
		submitButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				submitLogIn(v);
			}
		});
		EditText password = (EditText) findViewById(R.id.password);
		password.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					submitLogIn(v);
					return true;
				} else
					return false;
			}
		});
	}

	public void submitLogIn(View v) {
		String username = ((EditText) findViewById(R.id.username)).getText()
				.toString();

		String password = ((EditText) findViewById(R.id.password)).getText()
				.toString();
		dialog = ProgressDialog.show(LogInActivity.this, "",
				getString(R.string.loading_please_wait), true);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				LogInService.class);
		intent.putExtra("command", LogInService.GET_LOGIN_CMD);
		intent.putExtra("username", username);
		intent.putExtra("password", password);
		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				dialog.dismiss();

				if (resultCode == LogInService.USER_PASS_OK) {
					TextView error = (TextView) findViewById(R.id.error_log_in);
					error.setText("");
					Log.d(TAG, "OK");
					Intent intent = new Intent(LogInActivity.this,
							HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
					startActivityForResult(intent,0);
					setResult(FINISH); // Kills Main Activity
					LogInActivity.this.finish();
				} else if (resultCode == LogInService.STATUS_CONNECTION_ERROR) {
					Log.d(TAG, "Connection error.");
					Toast.makeText(getApplicationContext(),
							getString(R.string.connection_error),
							Toast.LENGTH_SHORT).show();
				} else if (resultCode == LogInService.USER_PASS_ERROR) {
					TextView error = (TextView) findViewById(R.id.error_log_in);
					error.setText(R.string.error_user_password);
					Log.d(TAG, "Wrong user/password");
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FINISH) {
			setResult(FINISH);
			LogInActivity.this.finish();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(OPTIONS_MENU_GROUP_ID, MAIN_ITEM_ID, Menu.FIRST, R.string.home)
				.setIcon(R.drawable.home);
		menu.add(OPTIONS_MENU_GROUP_ID, CATEGORY_ITEM_ID, Menu.FIRST + 1,
				R.string.categories).setIcon(R.drawable.categories);
		menu.add(OPTIONS_MENU_GROUP_ID, HELP_ITEM_ID, Menu.FIRST + 2,
				R.string.help).setIcon(R.drawable.help);

		return true; // True for the menu to be visible
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_ITEM_ID:
			goToMain();
			return true;
		case CATEGORY_ITEM_ID:
			showCategories();
			return true;
		case HELP_ITEM_ID:
			showHelp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showCategories() {
		Intent intent = new Intent(LogInActivity.this, CategoriesActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToMain() {
		Intent intent = new Intent(LogInActivity.this, MainActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_login))
				.setPositiveButton(android.R.string.ok, null).show();
	}

}
