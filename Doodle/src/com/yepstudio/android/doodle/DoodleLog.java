package com.yepstudio.android.doodle;

import android.util.Log;

public class DoodleLog {

	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	public int level = WARN;
	private String TAG = "";


	public static DoodleLog getInstance(String tag) {
		return new DoodleLog(tag);
	}

	public static DoodleLog getInstanceClazz(Class<?> clazz) {
		return getInstance(clazz.getSimpleName());
	}

	private DoodleLog(String tag) {
		super();
		TAG = tag;
	}

	public void d(String log) {
		if (DEBUG > level) {
			Log.d(TAG, log);
		}
	}

	public void i(String log) {
		if (INFO > level) {
			Log.i(TAG, log);
		}
	}

	public void w(String log) {
		if (WARN > level) {
			Log.w(TAG, log);
		}
	}

	public void e(String log, Throwable tr) {
		if (ERROR > level) {
			Log.e(TAG, log, tr);
		}
	}

}
