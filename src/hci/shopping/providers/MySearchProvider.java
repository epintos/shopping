package hci.shopping.providers;

import android.content.SearchRecentSuggestionsProvider;

public class MySearchProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "hci.shopping.providers.MySearchProvider";
	public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

	public MySearchProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
