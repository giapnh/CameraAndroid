package com.gnd.main.utils;

import android.util.Log;

public class D {
	public static final String TAG = "GND";
	public static final boolean debug = true;

	public static void i(String message) {
		if (debug)
			Log.i(TAG, message);
	}

	public static void e(String message) {
		if (debug)
			Log.e(TAG, message);
	}
}
