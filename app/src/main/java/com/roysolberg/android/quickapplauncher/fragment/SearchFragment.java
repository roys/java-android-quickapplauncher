package com.roysolberg.android.quickapplauncher.fragment;

import android.database.DatabaseUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.roysolberg.android.quickapplauncher.AppListFragment;
import com.roysolberg.android.quickapplauncher.DatabaseHelper;
import com.roysolberg.android.quickapplauncher.MainActivity;
import com.roysolberg.android.quickapplauncher.R;
import com.roysolberg.android.util.Logger;
import com.roysolberg.util.StringUtil;

public class SearchFragment extends AppListFragment implements OnQueryTextListener{

	private final static Logger logger = Logger.getLogger(SearchFragment.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	protected static final String MAX_NUM_OF_APPS = "100";
	
	protected SearchView searchView;
	protected String query;
	
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
		logger.debug("onCreateView()");
		View view = onCreateView(inflater, container, savedInstanceState, R.layout.fragment_search);
		searchView = (SearchView) view.findViewById(R.id.searchView);
	    searchView.setIconified(false);
	    searchView.setQueryHint("Start typing part of app name or description.");
	    searchView.setOnQueryTextListener(this);
		return view;
	}
	
	public void focusSearchWidget() {
		if(searchView != null){ // This one can be null if fragment isn't fully created. TODO: Should we focus/remove focus after it's created?
			searchView.setIconified(false);
			searchView.requestFocus();
		}
	}

	public void deFocusSearchWidget() {
		if(searchView != null) // This one can be null if fragment isn't fully created. TODO: Should we focus/remove focus after it's created?
			searchView.setIconified(true);
	}
	
	public void requery(){
		if(query != null){
			if(databaseHelper != null && simpleCursorAdapter != null){
				new Thread(new Runnable() {
					@Override
					public void run() {
						logger.debug("Perform search for [" + query + "]");
						final String selectionArg = "%" + query + "%";
						final String orderBy = DatabaseHelper.COLUMN_NAME + "=" + DatabaseUtils.sqlEscapeString(query) + ", " + DatabaseHelper.COLUMN_NAME + "=" + DatabaseUtils.sqlEscapeString(query+ "%") + ", " + DatabaseHelper.COLUMN_NAME + "=" + DatabaseUtils.sqlEscapeString("%" + query + "%") + ", "
								+ DatabaseHelper.COLUMN_DESCRIPTION + "=" + DatabaseUtils.sqlEscapeString(query) + ", " + DatabaseHelper.COLUMN_DESCRIPTION + "=" + DatabaseUtils.sqlEscapeString(query+ "%") + ", " + DatabaseHelper.COLUMN_DESCRIPTION + "=" + DatabaseUtils.sqlEscapeString("%" + query + "%");
						logger.debug("orderBy:" + orderBy);
						cursor = databaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_APPS, DatabaseHelper.COLUMNS_APPS, DatabaseHelper.COLUMN_NAME + " LIKE ? OR " + DatabaseHelper.COLUMN_DESCRIPTION + " LIKE ?", new String[]{selectionArg, selectionArg}, null, null, orderBy, MAX_NUM_OF_FREQ_APPS);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								simpleCursorAdapter.changeCursor(cursor);
							}
						});
					}
				}).start();
			}
		}
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		logger.debug("onQueryTextChange(" + newText + ")");
		newText = StringUtil.trim(newText);
		// TODO: Add delay to avoid too much searching?
		// TODO: Search for 1 letter words?
		if(!StringUtil.isEmpty(newText) && !newText.equals(query)){ // User is searching for something new (and not empty)
			query = newText;
			requery();
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		logger.debug("onQueryTextSubmit(" + query + ")");
		// TODO: What should we do here? Nothing?
		return false;
	}

}
