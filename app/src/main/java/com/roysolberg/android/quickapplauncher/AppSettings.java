package com.roysolberg.android.quickapplauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.roysolberg.android.util.Logger;

public class AppSettings {

	private final static Logger logger = Logger.getLogger(AppSettings.class.getSimpleName(), MainActivity.LOG_LEVEL);

	protected final static String KEY_HAS_RUN_APP = "has_run_app";
	protected final static String KEY_INDEX_TIMESTAMP = "index_timestamp";
	protected final static String KEY_START_APP_WITH_KEYS_VISIBLE = "start_app_with_keys_visible";
	
	protected SharedPreferences sharedPreferences;
	protected Editor editor;
	protected static AppSettings appSettings;
	
	protected AppSettings(Context context) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static AppSettings getInstance(Context context){
		if(appSettings == null){
			logger.debug("Instaniating app settings.");
			appSettings = new AppSettings(context);
		}
		return appSettings;
	}
	
	protected Editor getEditor(){
		if(editor == null){
			editor = sharedPreferences.edit();
		}
		return editor;
	}

	public boolean hasRunApp() {
		return sharedPreferences.getBoolean(KEY_HAS_RUN_APP, false);
	}

	public void setHasRunApp(boolean uploaded) {
		getEditor().putBoolean(KEY_HAS_RUN_APP, uploaded).commit();
	}

	public boolean startAppWithKeysVisible() {
		return sharedPreferences.getBoolean(KEY_START_APP_WITH_KEYS_VISIBLE, false);
	}

	public long getLastIndexTimestamp() {
		return sharedPreferences.getLong(KEY_INDEX_TIMESTAMP, 0);
	}

	public void setLastIndexTimestamp(long timestamp) {
		getEditor().putLong(KEY_INDEX_TIMESTAMP, timestamp).commit();
	}

}
