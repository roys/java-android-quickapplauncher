package com.roysolberg.android.quickapplauncher;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.roysolberg.android.quickapplauncher.fragment.RecentlyInstalledFragment;
import com.roysolberg.android.quickapplauncher.fragment.RecentlyUpdatedFragment;
import com.roysolberg.android.quickapplauncher.fragment.SearchFragment;
import com.roysolberg.android.quickapplauncher.receiver.AppChangeReceiver;
import com.roysolberg.android.util.Logger;
import com.roysolberg.android.util.Toaster;

public class MainActivity extends FragmentActivity implements OnPageChangeListener {

	private final static Logger logger = Logger.getLogger(MainActivity.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	protected static final int LOG_LEVEL_DEVEL = Log.DEBUG;
	protected static final int LOG_LEVEL_PROD =  Log.INFO;

	public static final boolean DEVEL_MODE = false; // XXX: Set to false for production
	public static final int LOG_LEVEL = DEVEL_MODE ? LOG_LEVEL_DEVEL : LOG_LEVEL_PROD;
	public static final String BROADCAST_ACTION_PACKAGE_ADDED = "com.roysolberg.android.quickapplauncher.PACKAGE_ADDED";
	public static final String BROADCAST_ACTION_PACKAGE_REMOVED = "com.roysolberg.android.quickapplauncher.PACKAGE_REMOVED";
	public static final String BROADCAST_ACTION_REMOVE_PACKAGE = "com.roysolberg.android.quickapplauncher.REMOVE_PACKAGE";
	
	protected static final int PAGE_SEARCH = 2;
	
	protected AppSettings appSettings;
	protected RecentlyUpdatedFragment recentlyUpdatedFragment;
	protected RecentlyInstalledFragment recentlyInstalledFragment;
	protected SearchFragment searchFragment;
	protected FrequentlyUsedFragment frequentlyUsedFragment;
	protected ProgressBar progressBar;
	protected InputMethodManager inputMethodManager;
	
	protected PagerAdapter pagerAdapter;
	protected ViewPager viewPager;
	protected SQLiteDatabase database;
	protected int currentPage = PAGE_SEARCH;
	protected AppChangeReceiver appChangeReceiver;
	protected UpdateFragmentReceiver updateFragmentReceiver;
	protected AppSearchAsyncTask appSearchAsyncTask;
	protected boolean firstRun;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		recentlyUpdatedFragment = new RecentlyUpdatedFragment();
		recentlyInstalledFragment = new RecentlyInstalledFragment();
		searchFragment = new SearchFragment();
		frequentlyUsedFragment = new FrequentlyUsedFragment();
		
		setContentView(R.layout.activity_main);

		pagerAdapter = new PagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(this);
		viewPager.setOffscreenPageLimit(3);
		viewPager.setCurrentItem(currentPage);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		appSettings = AppSettings.getInstance(getApplicationContext());
		firstRun = !appSettings.hasRunApp();
		
		try{
			database = new DatabaseHelper(getApplicationContext()).getWritableDatabase();
		}catch(SQLiteException e){
			logger.error("Got SQLiteException while trying to get a writeable version of [" + DatabaseHelper.DATABASE + "].");
			// TODO warn user;
		}
		startListeningForAppChanges();
		
		if(firstRun){
			init(); // Get crackin'
		}else{
			new Handler().postDelayed(new Runnable() { // Give UI a chance to settle to let user start using the app
				@Override
				public void run() {
					init();
				}
			}, 2000);
		}
	}
	
	protected void init(){
		appSearchAsyncTask = new AppSearchAsyncTask();
		appSearchAsyncTask.execute();
		loadAd();
	}
	
	@Override
	protected void onStop() {
		if(appSearchAsyncTask != null){ // Might be null if app is quickly terminated
			appSearchAsyncTask.cancel(true); // We cancel the task
		}
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		stopListeningForAppChanges();
		super.onDestroy();
	}
	
	protected void startListeningForAppChanges() {
		appChangeReceiver = new AppChangeReceiver();
		updateFragmentReceiver = new UpdateFragmentReceiver();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
		intentFilter.addDataScheme("package");
		registerReceiver(appChangeReceiver, intentFilter);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_ACTION_REMOVE_PACKAGE);
		intentFilter.addDataScheme("package");
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(appChangeReceiver, intentFilter);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_ACTION_PACKAGE_ADDED);
		intentFilter.addAction(BROADCAST_ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateFragmentReceiver, intentFilter);
	}
	
	protected void stopListeningForAppChanges(){
		unregisterReceiver(appChangeReceiver);
		try{
			unregisterReceiver(updateFragmentReceiver);
		}catch(IllegalArgumentException e){
			logger.warn("Got IllegalArgumentException while trying to unregister local receiver."); // TODO: Look into this issue.
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_item_search:
				viewPager.setCurrentItem(PAGE_SEARCH);
				searchFragment.focusSearchWidget();
				return true;
//			case R.id.menu_item_settings:
//				// TODO: settings with keyboard option and pay to remove ad and maybe rebuild app db? and remember tab?
//				return true;
			case R.id.menu_item_playStore:
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				} catch (android.content.ActivityNotFoundException e) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				}
				return true;
			default:
				return false;
		}
	}

	protected class PagerAdapter extends FragmentStatePagerAdapter { // TODO: use this vs FragmentPagerAdapter?
		
		public PagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position){
				case 0:
					return recentlyUpdatedFragment;
				case 1:
					return recentlyInstalledFragment;
				case 2:
					return searchFragment;
				case 3:
					return frequentlyUsedFragment;
				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch(position){
				case 0:
					return "Recently updated";
				case 1:
					return "Recently installed";
				case 2:
					return "Search";
				case 3:
					return "Frequently used";
				default:
					return "";
			}	
		}
		
	}

	public class AppSearchAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			long lastIndexTimestamp = appSettings.getLastIndexTimestamp();
			if((System.currentTimeMillis() - lastIndexTimestamp) > 5 * 60 * 1000){
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setIndeterminate(true);
				progressBar.setProgress(0);
				if(firstRun){ // First time user runs app
					Toaster.toastLong(getApplicationContext(), "Doing an initial indexing of all apps. This might take a couple of minutes.");
				}
			}else{
				logger.debug("Just did indexing. Not doing anything at this time.");
				cancel(true);
			}
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			ContentValues contentValues = new ContentValues();
			PackageManager packageManager = getApplicationContext().getPackageManager();
			List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
			final int packageCount = packageInfos.size();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progressBar.setMax(packageCount);
					progressBar.setIndeterminate(false);
				}
			});
			logger.debug("Found [" + packageCount + "] packages.");
			int numOfAppsAdded = 0;
			int numOfAppsAddFails = 0;
			int numOfAppsUpdated = 0;
			try{
				for (PackageInfo packageInfo : packageInfos) {
					if(isCancelled()){
						logger.debug("Task cancelled. Stopping package indexing.");
						break;
					}
					if(packageInfo.applicationInfo != null){
						if(!firstRun){
							// Doing a quick check if we have added the app before
							Cursor cursor = database.rawQuery("select 1 from " + DatabaseHelper.TABLE_APPS + " where " + DatabaseHelper.COLUMN_PACKAGE + "=?", new String[]{packageInfo.packageName});
							if(cursor.getCount() > 0){ // Existing app
								contentValues.clear();	
								contentValues.put(DatabaseHelper.COLUMN_NAME, String.valueOf(packageInfo.applicationInfo.loadLabel(packageManager)));
								CharSequence description = packageInfo.applicationInfo.loadDescription(packageManager);
								if(description != null)
									contentValues.put(DatabaseHelper.COLUMN_DESCRIPTION, description.toString());
								contentValues.put(DatabaseHelper.COLUMN_INSTALL_TIMESTAMP, packageInfo.firstInstallTime);
								contentValues.put(DatabaseHelper.COLUMN_UPDATE_TIMESTAMP, packageInfo.lastUpdateTime);
								int rowsAffected = database.update(DatabaseHelper.TABLE_APPS, contentValues, DatabaseHelper.COLUMN_PACKAGE + "=?", new String[]{packageInfo.packageName});
								if(rowsAffected == 0){
									numOfAppsAddFails++;
								}else{
									numOfAppsUpdated++;
								}
								publishProgress();
								continue;
							}
						}
						if(packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null){
							// TODO: open datadir?applicationInfo.dataDir
							contentValues.clear();
							contentValues.put(DatabaseHelper.COLUMN_PACKAGE, packageInfo.packageName);
							contentValues.put(DatabaseHelper.COLUMN_NAME, String.valueOf(packageInfo.applicationInfo.loadLabel(packageManager)));
							CharSequence description = packageInfo.applicationInfo.loadDescription(packageManager);
							if(description != null)
								contentValues.put(DatabaseHelper.COLUMN_DESCRIPTION, description.toString());
							contentValues.put(DatabaseHelper.COLUMN_INSTALL_TIMESTAMP, packageInfo.firstInstallTime);
							contentValues.put(DatabaseHelper.COLUMN_UPDATE_TIMESTAMP, packageInfo.lastUpdateTime);
							long id = database.insert(DatabaseHelper.TABLE_APPS, null, contentValues);
							if(id == -1){
								numOfAppsAddFails++;
							}else{
								numOfAppsAdded++;
							}
						}else{
							logger.debug("Skipped package [" + packageInfo.packageName + "] because it doesn't have an activity to launch.");	
						}
					}else{
						logger.debug("Skipped package [" + packageInfo.packageName + "] because it doesn't contain any app info.");
					}
					publishProgress();
				}
			}catch(SQLiteDatabaseLockedException e){
				logger.error("Got SQLiteDatabaseLockedException while trying to update database with app info. Not able to update database this time.", e);
			}
			logger.debug("Added [" + numOfAppsAdded + "] apps. Updated [" + numOfAppsUpdated + "] existing apps. [" + numOfAppsAddFails + "] adds/updates failed.");
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			progressBar.incrementProgressBy(1);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			logger.debug("onPostExecute()");
			progressBar.setVisibility(View.GONE);
			if(firstRun){
				Toaster.toastLong(getApplicationContext(), "Done indexing apps.");
				appSettings.setHasRunApp(true);
			}
			appSettings.setLastIndexTimestamp(System.currentTimeMillis());
			recentlyUpdatedFragment.requery();
			recentlyInstalledFragment.requery();
			searchFragment.requery();
			// We dont have to notify freq used fragment as none of the new apps are freq used
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		/* no-op */
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		/* no-op */
	}

	@Override
	public void onPageSelected(int page) {
		if(page != PAGE_SEARCH){
			if(currentPage == PAGE_SEARCH){
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						searchFragment.deFocusSearchWidget();
					}
				}, 500);
			}
		}
		currentPage = page;
	}
	
	protected void loadAd() {
		if(shouldDisplayAd()){
			AdView adView = (AdView) findViewById(R.id.adView);
			adView.setVisibility(View.VISIBLE);
			adView.loadAd(new AdRequest());
		}
	}
	
	protected boolean shouldDisplayAd() {
		if(appSettings.getNumberOfRuns() < 3){
			appSettings.incNumberOfRuns();
			return false;
		}
		return true;
	}

	public class UpdateFragmentReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			logger.debug("onReceive2(intent:[action:" + intent.getAction() + ",data:" + intent.getDataString() + "])");
			recentlyUpdatedFragment.requery();
			recentlyInstalledFragment.requery();
			searchFragment.requery();
			// We dont have to notify freq used fragment as none of the new apps are freq used
		}
	}

	public void updateFrequentlyStartedAppsFragment() {
		frequentlyUsedFragment.requery();
	}

}
