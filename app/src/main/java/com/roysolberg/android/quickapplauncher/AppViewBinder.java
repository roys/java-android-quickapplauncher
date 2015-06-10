package com.roysolberg.android.quickapplauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.ImageView;

import com.roysolberg.android.util.Logger;

public class AppViewBinder implements ViewBinder {

	private final static Logger logger = Logger.getLogger(AppViewBinder.class.getSimpleName(), MainActivity.LOG_LEVEL);

	protected PackageManager packageManager;
	protected int iconSize;

	public AppViewBinder(PackageManager packageManager, int iconSize) {
		this.packageManager = packageManager;
		this.iconSize = iconSize;
	}

	@Override
	public boolean setViewValue(View v, Cursor c, int index) {
		if (v instanceof ImageView) {
			String packageName = c.getString(index);
			try {
				ImageView imageView = (ImageView) v;
				imageView.setImageDrawable(packageManager.getApplicationIcon(packageName));
				imageView.getLayoutParams().width = iconSize;
				imageView.getLayoutParams().height = iconSize;
			} catch (NameNotFoundException e) {
				logger.warn("Got NameNotFoundException while trying to load app icon for package [" + packageName + "]. No icon will be shown. Sending broadcast to remove package.");
				Intent localIntent = new Intent(MainActivity.BROADCAST_ACTION_REMOVE_PACKAGE);
				localIntent.setData(Uri.parse("package:" + packageName));
				localIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
				LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(localIntent);
				((ImageView)v).setImageDrawable(null);
			}
			return true;
		}
		return false;
	}

}
