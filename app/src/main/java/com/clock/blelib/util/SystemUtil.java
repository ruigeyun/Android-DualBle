/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemUtil {
	public final static String IMSI = "IMSI";
	public final static String IMEI = "IMEI";
	private final static String TAG = "SystemUtil";
	private static long exitTime = 0;

	private static Context mContext;

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void exitApplication(Activity activity) {
		if((System.currentTimeMillis()-exitTime) > 2000){
			Toast.makeText(activity, "Press exit again", Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			activity.finish();
			android.os.Process.killProcess(android.os.Process.myPid()); // 获取PID
			System.exit(0);
		}
	}

	// 重启应用
	public static void restartApp(Activity context) {
		Intent intent = new Intent();
		// 参数1：包名，参数2：程序入口的activity
		intent.setClassName(context.getPackageName(),
				"com.gbd.sourcing.app.bleledconcurrent.view.Main2Activity");
		@SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(
				context.getApplicationContext(), 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000,
				restartIntent); // 2秒钟后重启应用
		context.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
	
	public static void restartApp(Context context) {
		Intent intent = new Intent();
		// 参数1：包名，参数2：程序入口的activity
		intent.setClassName(context.getPackageName(),
				"com.gbd.sourcing.app.bleledconcurrent.view.Main2Activity");
		@SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(
				context.getApplicationContext(), 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000,
				restartIntent); // 2秒钟后重启应用
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	// 重启系统
	public static void restartSys() {
		try {
            Process exeEcho = Runtime.getRuntime().exec("reboot");
        } catch (IOException e) {
             LogUtil.d("", "execCommand Excute exception: " + e.getMessage());
        }
	}
	
	// 关闭系统
	public static void shutdownSys() {
		try {
			Process exeEcho = Runtime.getRuntime().exec("reboot -p");
		} catch (IOException e) {
			LogUtil.d("", "execCommand Excute exception: " + e.getMessage());
		}
	}
	
    /**
     * 关闭输入法
     * @param view
     */
    public static boolean hideSoftInputFromWindow(View view) {
	    InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) { // 没用，一直是返回true
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			return true;
		}
		return false;
	}
    
    public static void getPhoneHardwareInfo(Context context) {
		getIMSIAndIMEI(context);
		getWindowWidth(context);
		getWindowHeigh(context);
		getScreenPixels(context);
		getScreenDensity(context);
		getScreenDp(context);
		getPhoneSoftwareInfo();
	}

	public static void getPhoneSoftwareInfo() {
		LogUtil.i(TAG, "获取手机 RELEASE: " + Build.VERSION.RELEASE);
		LogUtil.i(TAG, "获取手机 SDK_INT: " + Build.VERSION.SDK_INT);
		getAppVesionCode();
		getAppVersionName();
		getUniqueId();
	}
	/**
	 * 获取设备IMSI和IMEI号
	 * 
	 * @param context
	 * @return
	 */
	public static Map<String, String> getIMSIAndIMEI(Context context) {
		HashMap<String, String> map = new HashMap<String, String>();

		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
//		String imsi = mTelephonyMgr.getSubscriberId();
//		String imei = mTelephonyMgr.getDeviceId();
//
//		map.put(IMSI, imsi);
//		map.put(IMEI, imei);
		getPhoneBrand();
		getPhoneModel();
		LogUtil.i(TAG, "获取手机 ID: " + Build.ID);
		LogUtil.i(TAG, "获取手机 DISPLAY: " + Build.DISPLAY);
		LogUtil.i(TAG, "获取手机 PRODUCT: " + Build.PRODUCT);
		LogUtil.i(TAG, "获取手机 DEVICE: " + Build.DEVICE);
		LogUtil.i(TAG, "获取手机 BOARD: " + Build.BOARD);
		LogUtil.i(TAG, "获取手机 CPU_ABI: " + Build.CPU_ABI);
		LogUtil.i(TAG, "获取手机 CPU_ABI2: " + Build.CPU_ABI2);
//		LogUtil.i(TAG, "获取手机IMSI: " + imsi);
//		LogUtil.i(TAG, "获取手机IMEI: " + imei);
		return map;
	}

	//获取手机品牌
	private static String getPhoneBrand() {
		String brand = Build.BRAND;
		LogUtil.i(TAG, "获取手机 品牌: " + brand);
		return brand;
	}

	// 获取手机型号
	private static String getPhoneModel() {
		String model = Build.MODEL;
		LogUtil.i(TAG, "获取手机 型号: " + model);
		return model;
	}

	// 获取手机型号
	public static String getPhoneName() {

		return Build.BRAND + " " + Build.MODEL;
	}

//	private static String toMD5(String text) throws NoSuchAlgorithmException {
//		//获取摘要器 MessageDigest
//		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
//		//通过摘要器对字符串的二进制字节数组进行hash计算
//		byte[] digest = messageDigest.digest(text.getBytes());
//
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < digest.length; i++) {
//			//循环每个字符 将计算结果转化为正整数;
//			int digestInt = digest[i] & 0xff;
//			//将10进制转化为较短的16进制
//			String hexString = Integer.toHexString(digestInt);
//			//转化结果如果是个位数会省略0,因此判断并补0
//			if (hexString.length() < 2) {
//				sb.append(0);
//			}
//			//将循环结果添加到缓冲区
//			sb.append(hexString);
//		}
//		//返回整个结果
//		return sb.toString();
//	}

	public static String getUniqueId(){
//		String androidID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
//		LogUtil.i(TAG, "获取手机 androidID: " + androidID);
//		LogUtil.i(TAG, "获取手机 SERIAL: " + Build.SERIAL);
//		String id = androidID + Build.SERIAL;
//
//        String tomd5Str = MD5Util.encrypt(id);
//        LogUtil.i(TAG, "获取手机 tomd5Str: " + tomd5Str); // 20a3d89e13829dd8fe0fa0517790b37f
//        return tomd5Str;
        return "";
	}

	//	public static String getDeviceId(Context context) {
//		  StringBuilder deviceId = new StringBuilder();
//		  // 渠道标志
//		  deviceId.append("a");
//		  try {
//		    //wifi mac地址
//		    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//		    WifiInfo info = wifi.getConnectionInfo();
//		    String wifiMac = info.getMacAddress();
//		    if(!isEmpty(wifiMac)){
//		      deviceId.append("wifi");
//		      deviceId.append(wifiMac);
//		      PALog.e("getDeviceId : ", deviceId.toString());
//		      return deviceId.toString();
//		    }
//		    //IMEI（imei）
//		    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//		    String imei = tm.getDeviceId();
//		    if(!isEmpty(imei)){
//		      deviceId.append("imei");
//		      deviceId.append(imei);
//		      PALog.e("getDeviceId : ", deviceId.toString());
//		      return deviceId.toString();
//		    }
//		    //序列号（sn）
//		    String sn = tm.getSimSerialNumber();
//		    if(!isEmpty(sn)){
//		      deviceId.append("sn");
//		      deviceId.append(sn);
//		      PALog.e("getDeviceId : ", deviceId.toString());
//		      return deviceId.toString();
//		    }
//		    //如果上面都没有， 则生成一个id：随机码
//		    String uuid = getUUID(context);
//		    if(!isEmpty(uuid)){
//		      deviceId.append("id");
//		      deviceId.append(uuid);
//		      PALog.e("getDeviceId : ", deviceId.toString());
//		      return deviceId.toString();
//		    }
//		  } catch (Exception e) {
//		    e.printStackTrace();
//		    deviceId.append("id").append(getUUID(context));
//		  }
//		  PALog.e("getDeviceId : ", deviceId.toString());
//		  return deviceId.toString();
//		}
//		/**
//		 * 得到全局唯一UUID
//		 */
//		public static String getUUID(Context context){
//		  SharedPreferences mShare = getSysShare(context, "sysCacheMap");
//		  if(mShare != null){
//		    uuid = mShare.getString("uuid", "");
//		  }
//		  if(isEmpty(uuid)){
//		    uuid = UUID.randomUUID().toString();
//		    saveSysMap(context, "sysCacheMap", "uuid", uuid);
//		  }
//		  PALog.e(tag, "getUUID : " + uuid);
//		return uuid;
//		}

	//屏幕像素密度
	public  static float getScreenDensity(Context context){
		WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		float density = dm.density;
		LogUtil.i(TAG, "获取手机 density: " + density);
		LogUtil.i(TAG, "获取手机 density2: " + context.getResources().getDisplayMetrics().density);
		LogUtil.i(TAG, "获取手机 densityDpi: " + dm.densityDpi);
		return density;
	}

	//屏幕像素密度
	public  static int getScreenDp(Context context){
		Configuration config = context.getResources().getConfiguration();
		int  smallestScreenWidth = config.smallestScreenWidthDp;
		LogUtil.i(TAG, "获取手机 config: " + config.toString());
		LogUtil.i(TAG, "获取手机 ScreenDp: " + smallestScreenWidth);
		return smallestScreenWidth;
	}

	public static int getWindowWidth(Context context){
		// 获取屏幕分辨率
		WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int mScreenWidth = dm.widthPixels;
		LogUtil.i(TAG, "获取屏幕Width: " + mScreenWidth);
		return mScreenWidth;
	}
	
	public static int getWindowHeigh(Context context){
		// 获取屏幕分辨率
		WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int mScreenHeigh = dm.heightPixels;
		LogUtil.i(TAG, "获取屏幕Heigh: " + mScreenHeigh);
		return mScreenHeigh;
	}
	/**
	 * 获取设备分辨率
	 * 
	 * @return
	 */
	public static void getScreenPixels(Context context) {
		WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		LogUtil.i(TAG, "获取设备分辨率: " + dm.toString());
//		return activity.getWindowManager().getDefaultDisplay();
	}

	/**
	 * 获取程序版本号
	 * 
	 * @throws NameNotFoundException
	 * 
	 */
	public static float getAppVesionCode(){
		try {
			float code = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), Context.MODE_PRIVATE).versionCode;
			LogUtil.i(TAG, "获取AppVesionCode: " + code);
			return code;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 获取程序版本名称
	 * 
	 * @throws NameNotFoundException
	 * 
	 */
	public static String getAppVersionName() {
		try {
			String name = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), Context.MODE_PRIVATE).versionName;
			LogUtil.i(TAG, "获取AppVesionName: " + name);
			return name;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 获取系统版本号
	 * 
	 * @throws NameNotFoundException
	 * 
	 */
	public static String getSystemVesionCode() {
		// return ((TelephonyManager)
		// context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceSoftwareVersion();
		LogUtil.i(TAG, "获取SystemVesionCode: " + Build.VERSION.RELEASE);
		return Build.VERSION.RELEASE;
	}

	/**
	 * 获取设备类型(phone、pad)
	 * 
	 */
	public static String deviceStyle(Context context) {
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int type = telephony.getPhoneType();
		if (type == TelephonyManager.PHONE_TYPE_NONE) {
			return "phone";
		}

		return "pad";
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 *
	 * @param pxValue
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 *
	 * @param spValue
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * 获取应用环境
	 * 
	 * @param context
	 * @return
	 */
	public static String getEnvironment(Context context) {
		try {
			ApplicationInfo applicationInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Object object = (Object)applicationInfo.metaData.get("environment");
            return object.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/************** 判断app是否在最前端 *************/
	public static boolean isAppOnForeground(Context context) {
		// 最大运行任务数设置为1
		ActivityManager mActivityManager = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningTaskInfo> taskInfos = mActivityManager.getRunningTasks(1);
		if (taskInfos.size() > 0 && TextUtils.equals(context.getPackageName(),
						taskInfos.get(0).topActivity.getPackageName())) {
			return true;
		}
		return false;
	}
	
	/**
     * 获取媒体文件播放时间，格式化输出
     *
     * @param ms 毫秒
     * @return 格式化后的结果：hh:mm:ss
     */
    public static String getMediaTime(int ms) {
        int hour, mintue, second;

        //计算小时 1 h = 3600000 ms
        hour = ms / 3600000;

        //计算分钟 1 min = 60000 ms
        mintue = (ms - hour * 3600000) / 60000;

        //计算秒钟 1 s = 1000 ms
        second = (ms - hour * 3600000 - mintue * 60000) / 1000;

        //格式化输出，补零操作
        String sHour, sMintue, sSecond;
        if (hour < 10) {
            sHour = "0" + String.valueOf(hour);
        } else {
            sHour = String.valueOf(hour);
        }

        if (mintue < 10) {
            sMintue = "0" + String.valueOf(mintue);
        } else {
            sMintue = String.valueOf(mintue);
        }

        if (second < 10) {
            sSecond = "0" + String.valueOf(second);
        } else {
            sSecond = String.valueOf(second);
        }

        return sHour + ":" + sMintue + ":" + sSecond;
    }

    /**
     * <br>功能简述:4.4及以上获取图片的方法
     * <br>功能详细描述:
     * <br>注意:
     *
     * @param context 上下文
     * @param uri     待解析的 Uri
     * @return 真实路径
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
