package com.roysolberg.android.quickapplauncher;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.roysolberg.android.util.Logger;
import com.roysolberg.android.util.Toaster;

public abstract class AppListFragment extends Fragment implements OnItemClickListener {

	private final static Logger logger = Logger.getLogger(AppListFragment.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	protected static final String MAX_NUM_OF_FREQ_APPS = "100";
	
	protected Cursor cursor; // TODO do we need this one?
	protected SimpleCursorAdapter simpleCursorAdapter;
	protected DatabaseHelper databaseHelper;
	protected ListView listView;
	protected PackageManager packageManager;
	
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState, int layoutId) {
		logger.debug("onCreateView()");
		ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		final int iconSize = am.getLauncherLargeIconSize();
		packageManager = getActivity().getPackageManager();
		ViewBinder viewBinder = new AppViewBinder(packageManager, iconSize);
		databaseHelper = new DatabaseHelper(getActivity());
		simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_app, null, new String[]{DatabaseHelper.COLUMN_PACKAGE, DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_DESCRIPTION}, new int[] { R.id.imageview, R.id.textView_line1, R.id.textView_line2 }, 0);
		simpleCursorAdapter.setViewBinder(viewBinder);
		View view = inflater.inflate(layoutId, container, false);
	    listView = (ListView) view.findViewById(R.id.listView);
//	    listView.setEmptyText("No apps started from this app yet."); // TODO: fix
	    listView.setOnItemClickListener(this);
	    registerForContextMenu(listView);
	    listView.setAdapter(simpleCursorAdapter);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		logger.debug("onActivityCreated()");
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		handleAction(position, R.id.menu_item_startApp);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}
	
	public abstract void requery();
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!getUserVisibleHint()) { // We're called even though we're not being used
	        return false;
	    }
		int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		return handleAction(position, item.getItemId());
	}
	
	protected boolean handleAction(int position, int action){
		if(cursor.moveToPosition(position)){
			int index = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
			long dbId = cursor.getLong(index);
			index = cursor.getColumnIndex(DatabaseHelper.COLUMN_PACKAGE);
			String packageName = cursor.getString(index);
			index = cursor.getColumnIndex(DatabaseHelper.COLUMN_NUM_OF_RUNS);
			long numOfRuns = cursor.getLong(index) + 1;
			switch(action){
				case R.id.menu_item_startApp:
					Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
					if(intent != null){
						ContentValues contentValues = new ContentValues();
						contentValues.put(DatabaseHelper.COLUMN_NUM_OF_RUNS, numOfRuns);
						databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_APPS, contentValues, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(dbId)});
						((MainActivity)getActivity()).updateFrequentlyStartedAppsFragment();
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getActivity().startActivity(intent);
					}else{
						Toaster.toastLong(getActivity(), "App does not seem to have a screen to launch.");
					}
					return true;
				case R.id.menu_item_googlePlay:
					try {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					} catch (android.content.ActivityNotFoundException e) {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
					return true;
				case R.id.menu_item_appDetails:
					Uri packageUri = Uri.parse("package:" + packageName);
			        intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", packageUri);
			        try{
			        	startActivity(intent);
			        }catch (ActivityNotFoundException e) {
			        	Toaster.toastLong(getActivity(), "Unable to load app details.");
					}
					return true;
			}
		}else{ // Off position with cursor
			// TODO: handle this one for context menu or item click?
		}
		return false;
	}

}
