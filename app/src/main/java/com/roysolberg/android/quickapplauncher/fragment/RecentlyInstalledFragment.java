package com.roysolberg.android.quickapplauncher.fragment;

import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roysolberg.android.quickapplauncher.AppListFragment;
import com.roysolberg.android.quickapplauncher.DatabaseHelper;
import com.roysolberg.android.quickapplauncher.MainActivity;
import com.roysolberg.android.quickapplauncher.R;
import com.roysolberg.android.util.Logger;

public class RecentlyInstalledFragment extends AppListFragment {

	private final static Logger logger = Logger.getLogger(RecentlyInstalledFragment.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	protected static final long RECENTLY_AGE = 60 * 24 * 60 * 60 * 1000;
	protected static final String MAX_NUM_OF_RECENT_APPS = "50";
	
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
			long installTime = new Date().getTime() - RECENTLY_AGE;
			cursor = databaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_APPS, DatabaseHelper.COLUMNS_APPS, DatabaseHelper.COLUMN_INSTALL_TIMESTAMP + ">=" + installTime , null, null, null, DatabaseHelper.COLUMN_INSTALL_TIMESTAMP + " DESC", MAX_NUM_OF_RECENT_APPS);
			simpleCursorAdapter.changeCursor(cursor);
		}
	}
	
}
