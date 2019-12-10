/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BaseActivity extends Activity {
	protected String TAG = "BaseActivity";

 	private Dialog mDialog;
 	private ProgressDialog bigDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Log.w(TAG, "onCreate.");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.d(TAG, "onRestart..");
	}

	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	
    	super.onRestoreInstanceState(savedInstanceState);
    }

	@Override
	protected void onResume() {
		super.onResume();
//		readSystemMember();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop...");
	}

	@Override
    protected void onDestroy() {
    	super.onDestroy();
    	showProgressBase(false);
		Log.e(TAG, "onDestroy....");
    }

	@Override
	public Resources getResources() {
		Resources resources = super.getResources();
		if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
			android.content.res.Configuration configuration = resources.getConfiguration();
			configuration.fontScale = 1.0f;
			resources.updateConfiguration(configuration, resources.getDisplayMetrics());
		}
		return resources;
	}
    /**
     * 自定义启动动画
     */
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }
    /**
     * 自定义启动动画
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
    	super.startActivityForResult(intent, requestCode);
    }
    /**
     * 自定义启动动画
     */
    @Override
    public void finish() {
        super.finish();
    }

	public synchronized void showProgressBase(boolean show) {
		if (mDialog != null && mDialog.isShowing()) { // 正在显示，
			mDialog.dismiss();
			mDialog.cancel();
			mDialog = null;
		}
		
		if (show) { // 必须在UI线程
			BaseActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mDialog = new Dialog(BaseActivity.this, R.style.framework_dialog_parent_style);
//					mDialog.setContentView(R.layout.dialog_framework);
					mDialog.setContentView(R.layout.dialog_loading2);
					mDialog.setCancelable(true);
					mDialog.show();
				}
			});
			
		} else {
			if (mDialog != null) {
				mDialog.dismiss();  // 不必在UI线程
				mDialog.cancel();
				mDialog = null;
			}
		}
	}

	public synchronized void showBigProgressBase(boolean show, final int content) {
		if (bigDialog != null && bigDialog.isShowing()) { // 正在显示，
			bigDialog.dismiss();
			bigDialog.cancel();
			bigDialog = null;
		}
		
		if (show) { // 必须在UI线程
			BaseActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(content == 0) {
						bigDialog = new ProgressDialog(BaseActivity.this);
					}
					else {
						bigDialog = new ProgressDialog(BaseActivity.this, content);
					}
					bigDialog.show();
				}
			});
			
		} else {
			if (bigDialog != null) {
				bigDialog.dismiss();  // 不必在UI线程
				bigDialog.cancel();
				bigDialog = null;
			}
		}
	}

	public void showToastNoUiBase(final int resId) {
		BaseActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void showToastNoUiBase(final String content) {
		BaseActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
