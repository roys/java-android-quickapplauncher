package com.roysolberg.android.util;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

	public static void toastLong(Context context, int redId) {
		Toast.makeText(context, redId, Toast.LENGTH_LONG).show();
	}

	public static void toastShort(Context context, int redId) {
		Toast.makeText(context, redId, Toast.LENGTH_SHORT).show();
	}

	public static void toastLong(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	public static void toastShort(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

}
