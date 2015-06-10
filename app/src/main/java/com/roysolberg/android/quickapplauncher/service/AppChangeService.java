package com.roysolberg.android.quickapplauncher.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.content.LocalBroadcastManager;

import com.roysolberg.android.quickapplauncher.DatabaseHelper;
import com.roysolberg.android.quickapplauncher.MainActivity;
import com.roysolberg.android.util.Logger;

public class AppChangeService extends IntentService {

	private final static Logger logger = Logger.getLogger(AppChangeService.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	protected SQLiteDatabase database;
	
	public AppChangeService() {
		super("App change service");
		logger.debug("AppChangeService()");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		try{
			database = new DatabaseHelper(getApplicationContext()).getWritableDatabase();
		}catch(SQLiteException e){
			logger.error("Got SQLiteException while trying to get a writeable version of [" + DatabaseHelper.DATABASE + "].");
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		logger.debug("onHandleIntent(intent:[action:" + intent.getAction() + ",data:" + intent.getDataString() + "])");
		if(database == null){
			return;
		}
		String packageName = intent.getData().getSchemeSpecificPart();
		if(Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())){
			PackageManager packageManager = getPackageManager();
			if(packageManager.getLaunchIntentForPackage(packageName) != null){
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
					ContentValues contentValues = new ContentValues();
					contentValues.put(DatabaseHelper.COLUMN_PACKAGE, packageName);
					contentValues.put(DatabaseHelper.COLUMN_NAME, String.valueOf(packageInfo.applicationInfo.loadLabel(packageManager)));
					CharSequence description = packageInfo.applicationInfo.loadDescription(packageManager);
					if(description != null)
						contentValues.put(DatabaseHelper.COLUMN_DESCRIPTION, description.toString());
					contentValues.put(DatabaseHelper.COLUMN_INSTALL_TIMESTAMP, packageInfo.firstInstallTime);
					contentValues.put(DatabaseHelper.COLUMN_UPDATE_TIMESTAMP, packageInfo.lastUpdateTime);
					long id = database.insertWithOnConflict(DatabaseHelper.TABLE_APPS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
					if(id != -1){
						logger.debug("Added package [" + packageName + "].");
						Intent localIntent = new Intent(MainActivity.BROADCAST_ACTION_PACKAGE_ADDED);
						localIntent.setData(intent.getData());
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
					}else{
						logger.warn("Failed to add package [" + packageName + "] to database. Ignoring package.");
					}
				} catch (NameNotFoundException e) { // Probably won't happen
					logger.error("Got NameNotFoundException while trying to get package info for package [" + packageName + "].");
				}
			}	
		}else if(Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction()) || MainActivity.BROADCAST_ACTION_REMOVE_PACKAGE.equals(intent.getAction())){
			int rowsAffected = database.delete(DatabaseHelper.TABLE_APPS, DatabaseHelper.COLUMN_PACKAGE + "=?", new String[]{packageName});
			if(rowsAffected > 0){
				logger.debug("Removed package [" + packageName + "].");
				Intent localIntent = new Intent(MainActivity.BROADCAST_ACTION_PACKAGE_REMOVED);
				localIntent.setData(intent.getData());
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
			}else{
				// Probably a package without a launch intent
			}
		}
	}
	
	@Override
	public void onDestroy() {
		database.close();
		super.onDestroy();
	}

}
