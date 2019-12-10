/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.utils;

import android.util.Log;

import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * 为了方便操作log我们需要自己定义个log类然后在开发阶段将下面LOG_LEVEL 设置为6这样所有的log都能显示，在发布的时候我们将LOG_LEVEL
 * 设置为0.这样log就非常方便管理了
 *
 * @author Administrator
 * 
 */
public final class LogUtil {
	public static int LOG_LEVEL = 6;
	public static int ERROR = 1;
	public static int WARN = 2;
	public static int INFO = 3;
	public static int DEBUG = 4;
	public static int VERBOS = 5;

	public static List<String> tagArray = new ArrayList<String>();
	
	public static void setLogLevel(int level) {
		LOG_LEVEL = level;
//		tagArray.add("BitmapManage");
	}
	
	public static void e(String tag, String msg) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > ERROR) {
			Log.e(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > WARN) {
			Log.w(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > INFO) {
			Log.i(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > VERBOS) {
			Log.v(tag, msg);
		}
	}

	public static void e(String tag, String msg, BLEAppDevice device) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > ERROR) {
			Log.e(tag, msg + " " + device.mDeviceId + " " + device.mBleAddress);
		}
	}

	public static void w(String tag, String msg, BLEAppDevice device) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > WARN) {
			Log.w(tag, msg + " " + device.mDeviceId + " " + device.mBleAddress);
		}
	}

	public static void i(String tag, String msg, BLEAppDevice device) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > INFO) {
			Log.i(tag, msg + " " + device.mDeviceId + " " + device.mBleAddress);
		}
	}

	public static void d(String tag, String msg, BLEAppDevice device) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > DEBUG) {
			Log.d(tag, msg + " " + device.mDeviceId + " " + device.mBleAddress);
		}
	}

	public static void v(String tag, String msg, BLEAppDevice device) {
		if(!isTagEnable(tag)) {
			return;
		}
		if (LOG_LEVEL > VERBOS) {
			Log.v(tag, msg + " " + device.mDeviceId + " " + device.mBleAddress);
		}
	}

	private static boolean isTagEnable(String Tag) {
		if(tagArray.contains(Tag)) {
			return false;
		}
		return true;
	}
}
