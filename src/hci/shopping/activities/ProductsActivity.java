package hci.shopping.activities;

import hci.shopping.R;
import hci.shopping.model.api.Product;
import hci.shopping.model.impl.ProductProviderImpl;
import hci.shopping.services.OrderUpdateService;
import hci.shopping.services.ProductsService;

import java.util.ArrayList;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProductsActivity extends ListActivity {
	private String TAG = getClass().getSimpleName();
	private final int MAIN_ITEM_ID = 1;
	private final int HOME_ITEM_ID = 2;
	private final int CATEGORY_ITEM_ID = 3;
	private final int LOG_OUT_ITEM_ID = 4;
	private final int HELP_ITEM_ID = 5;
	private final int OPTIONS_MENU_GROUP_ID = 1;
	private final int FINISH = -1;
	private int category_id;
	private int subcategory_id;
	private ProductAdapter productAdapter;
	private ArrayList<Product> products = new ArrayList<Product>();
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		category_id = new Integer(this.getIntent().getExtras()
				.getString("category_id"));
		subcategory_id = new Integer(this.getIntent().getExtras()
				.getString("subcategory_id"));

		setTitle(new String(this.getIntent().getExtras()
				.getString("bread_crumb")));

		dialog = ProgressDialog.show(ProductsActivity.this, "",
				getString(R.string.loading_please_wait), true);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
				ProductsService.class);
		intent.putExtra("command", ProductsService.GET_PRODUCTS_CMD);
		intent.putExtra("subcategory_id", subcategory_id);
		intent.putExtra("category_id", category_id);
		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				super.onReceiveResult(resultCode, resultData);
				dialog.dismiss();
				setContentView(R.layout.products);
				productAdapter = new ProductAdapter(ProductsActivity.this,
						R.layout.product_item, products);
				setListAdapter(productAdapter);
				if (resultCode == ProductsService.STATUS_OK) {

					Log.d(TAG, "OK");

					@SuppressWarnings("unchecked")
					ArrayList<Product> list = (ArrayList<Product>) resultData
							.getSerializable("return");
					 String result_num = (String) resultData
							.getSerializable("results_size");
					TextView name = (TextView) findViewById(R.id.products_found);
					name.setText(getString(R.string.products_found) + " "
							+ result_num);
					productsList(new ProductProviderImpl(list));
				} else if (resultCode == ProductsService.STATUS_CONNECTION_ERROR) {
					Log.d(TAG, "Connection error.");
					Toast.makeText(getApplicationContext(),
							getString(R.string.connection_error),
							Toast.LENGTH_SHORT).show();
					TextView name = (TextView) findViewById(R.id.products_found);
					name.setText(getString(R.string.products_found) + " "
							+ "0");
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

	private void productsList(ProductProviderImpl productProvider) {
		for (Product product : productProvider.getProducts()) {
			products.add(product);
		}
		productAdapter.notifyDataSetChanged();
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setCacheColorHint(0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);

		Bundle bundle = new Bundle();
		bundle.putString("product_id", ((Product) o).getID() + "");
		bundle.putString("bread_crumb", getTitle().toString());

		Intent newIntent = new Intent(ProductsActivity.this,
				ProductInfoActivity.class);
		newIntent.putExtras(bundle);
		startActivityForResult(newIntent, 0);

	}

	private class ProductAdapter extends ArrayAdapter<Product> {

		private ArrayList<Product> products;

		public ProductAdapter(Context context, int textViewResourceId,
				ArrayList<Product> items) {
			super(context, textViewResourceId, items);
			this.products = items;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.product_item, null);
			}
			Product o = products.get(position);
			if (o != null) {
				TextView name = (TextView) view.findViewById(R.id.product_name);
				RatingBar rank_place = (RatingBar) view
						.findViewById(R.id.product_ranking);
				TextView price = (TextView) view
						.findViewById(R.id.product_price);
				ImageView image = (ImageView) view
						.findViewById(R.id.product_image);

				if (image != null) {
					image.setImageDrawable(o.getImage());
				}
				if (name != null) {
					name.setText(o.getName());
				}
				if (rank_place != null) {
					rank_place.setMax(5);
					float ranking = Float.parseFloat(o.getRanking());
					if (ranking > 100)
						ranking = 5;
					else if (ranking > 50)
						ranking = new Float(3.5);
					else
						ranking = 2;
					rank_place.setRating(ranking);
				}
				if (price != null) {
					price.setText("$" + o.getPrice());
				}
			}
			return view;
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
			ProductsActivity.this.finish();
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
		Intent intent = new Intent(ProductsActivity.this,
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
		Intent intent = new Intent(ProductsActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		startActivityForResult(intent, 0);
		setResult(FINISH);
		ProductsActivity.this.finish();
	}

	public void goToMain() {
		Intent intent = new Intent(ProductsActivity.this, MainActivity.class);
		startActivityForResult(intent, 0);
	}

	public void goToHome() {
		Intent intent = new Intent(ProductsActivity.this, HomeActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showHelp() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.help))
				.setMessage(getString(R.string.help_products))
				.setPositiveButton(android.R.string.ok, null).show();
	}
}
