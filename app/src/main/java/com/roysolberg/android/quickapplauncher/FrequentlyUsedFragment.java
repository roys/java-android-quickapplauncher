package com.roysolberg.android.quickapplauncher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roysolberg.android.util.Logger;

public class FrequentlyUsedFragment extends AppListFragment {

	private final static Logger logger = Logger.getLogger(FrequentlyUsedFragment.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	protected static final String MAX_NUM_OF_FREQ_APPS = "50";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return onCreateView(inflater, container, savedInstanceState, R.layout.fragment_app_list);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		logger.debug("onActivityCreated()");
		requery();
	}

	public void requery() {
		if(databaseHelper != null && simpleCursorAdapter != null){
			cursor = databaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_APPS, DatabaseHelper.COLUMNS_APPS, DatabaseHelper.COLUMN_NUM_OF_RUNS + ">0", null, null, null, DatabaseHelper.COLUMN_NUM_OF_RUNS + " DESC", MAX_NUM_OF_FREQ_APPS);
			simpleCursorAdapter.swapCursor(cursor);
		}
	}
	
}
