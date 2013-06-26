package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.model.api.ProductInfo;
import hci.shopping.model.impl.ProductInfoProviderImpl;
import hci.shopping.services.OrderUpdateService;
import hci.shopping.services.ProductInfoService;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductInfoActivity extends Activity {
	private String TAG = getClass().getSimpleName();
	private final int MAIN_ITEM_ID = 1;
	private final int HOME_ITEM_ID = 2;
	private final int CATEGORY_ITEM_ID = 3;
	private final int LOG_OUT_ITEM_ID = 4;
	private final int HELP_ITEM_ID = 5;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private int product_id;
	private String category_id;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		product_id = new Integer(this.getIntent().getExtras()
				.getString("product_id"));

		setTitle(new String(this.getIntent().getExtras()
				.getString("bread_crumb")));

		dialog = ProgressDialog.show(ProductInfoActivity.this, "",
				getString(R.string.loading_please_wait), true);

		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				ProductInfoService.class);
		intent.putExtra("command", ProductInfoService.GET_PRODUC_INFO_CMD);
		intent.putExtra("product_id", product_id);
		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				dialog.dismiss();
				if (resultCode == ProductInfoService.STATUS_OK) {

					Log.d(TAG, "OK");

					@SuppressWarnings("unchecked")
					List<ProductInfo> list = (List<ProductInfo>) resultData
							.getSerializable("return");

					category_id = (String) resultData
							.getSerializable("category_id");
					productInfoList(new ProductInfoProviderImpl(list.get(0)));

				} else if (resultCode == ProductInfoService.STATUS_CONNECTION_ERROR) {
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

	private void productInfoList(ProductInfoProviderImpl productInfoProvider) {
		ProductInfo product = productInfoProvider.getProduct();
		setProductInformation(product);
	}

	private void setProductInformation(ProductInfo product) {
		if (category_id.equals("1"))
			setDVDInformation(product);
		else
			setBookInformation(product);

	}

	private void setDVDInformation(ProductInfo product) {
		setContentView(R.layout.product_info_dvd);
		if (product != null) {
			TextView actors = (TextView) findViewById(R.id.product_actors);
			TextView format = (TextView) findViewById(R.id.product_format);
			TextView name = (TextView) findViewById(R.id.product_name);
			TextView price = (TextView) findViewById(R.id.product_price);
			TextView run_time = (TextView) findViewById(R.id.product_run_time);
			TextView subtitles = (TextView) findViewById(R.id.product_subtitles);

			if (actors != null) {
				actors.setText(product.getActors());
			}
			if (format != null) {
				format.setText(product.getFormat());
			}
			if (name != null) {
				name.setText(product.getName());
			}
			if (price != null) {
				price.setText("$" + product.getPrice());
			}
			if (run_time != null) {
				run_time.setText(product.getRunTime() + " "
						+ getString(R.string.minutes));
			}
			if (subtitles != null) {
				subtitles.setText(product.getSubtitles());
			}

			ImageView image = (ImageView) findViewById(R.id.product_image);
			if (image != null) {
				image.setImageDrawable(product.getImage());
			}
		}
	}

	private void setBookInformation(ProductInfo product) {
		setContentView(R.layout.product_info_book);
		if (product != null) {
			TextView authors = (TextView) findViewById(R.id.product_authors);
			TextView publisher = (TextView) findViewById(R.id.product_publisher);
			TextView name = (TextView) findViewById(R.id.product_name);
			TextView price = (TextView) findViewById(R.id.product_price);
			TextView published_date = (TextView) findViewById(R.id.product_published_date);
			TextView language = (TextView) findViewById(R.id.product_language);
			if (authors != null) {
				authors.setText(product.getAuthors());
			}
			if (publisher != null) {
				publisher.setText(product.getPublisher());
			}
			if (name != null) {
				name.setText(product.getName());
			}
			if (price != null) {
				price.setText("$" + product.getPrice());
			}
			if (published_date != null) {
				published_date.setText(product.getPublishedDate());
			}
			if (language != null) {
				language.setText(product.getLanguage());
			}

			ImageView image = (ImageView) findViewById(R.id.product_image);
			if (image != null) {
				image.setImageDrawable(product.getImage());
			}
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FINISH) {
			setResult(FINISH);
			ProductInfoActivity.this.finish();
		}
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

	public void showCategories() {
		Intent intent = new Intent(ProductInfoActivity.this,
				CategoriesActivity.class);
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
		Intent intent = new Intent(ProductInfoActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		ProductInfoActivity.this.finish();
	}

	public void goToMain() {
		Intent intent = new Intent(ProductInfoActivity.this, MainActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToHome() {
		Intent intent = new Intent(ProductInfoActivity.this, HomeActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_product))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}