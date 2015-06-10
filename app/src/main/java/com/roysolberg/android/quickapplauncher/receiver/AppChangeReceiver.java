package com.roysolberg.android.quickapplauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.roysolberg.android.quickapplauncher.MainActivity;
import com.roysolberg.android.quickapplauncher.service.AppChangeService;
import com.roysolberg.android.util.Logger;

public class AppChangeReceiver extends BroadcastReceiver {

	private final static Logger logger = Logger.getLogger(AppChangeReceiver.class.getSimpleName(), MainActivity.LOG_LEVEL);
	
	@Override
	public void onReceive(Context context, Intent intent) {
		logger.debug("onReceive(intent:[action:" + intent.getAction() + ",data:" + intent.getDataString() + "])");
		Intent serviceIntent = new Intent(context, AppChangeService.class);
		serviceIntent.setAction(intent.getAction());
		serviceIntent.setData(intent.getData());
		context.startService(serviceIntent);
	}

}
