/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel.led;

import android.util.Log;

import com.clock.bluetoothlib.logic.network.DeviceInfoSyncListener;
import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;

public class BLELedNecessaryBiz {
    private final String TAG = "BLEOwnerBiz";

    private BLEAppDevice mOwnerDevice;
    private int reSendCnt = 0;
    private DeviceInfoSyncListener listener;

    public BLELedNecessaryBiz(BLEAppDevice bleDevice, final DeviceInfoSyncListener listener) {
        mOwnerDevice = bleDevice;
        this.listener = listener;
    }

    public void getDeviceInfo() {
        Log.d(TAG, "getDeviceInfo: "  + " " + mOwnerDevice.mDeviceId);
//        final Cmd73Version cmd = new Cmd73Version();
//        DataSendTransferBle.writeBleDevice(cmd, mOwnerDevice.mDeviceId, new OnRespResultListener() {
//            @Override
//            public void onSuccess(int code) {
//                ((LedDevice)mOwnerDevice).hardwareVersion = cmd.getVersion();
//                ((LedDevice)mOwnerDevice).dualColor = cmd.getDualColor();
//                ((LedDevice)mOwnerDevice).powerState = 0;
//
//                mOwnerDevice.updateDeviceInfo(null);
//
//                reSendCnt = 0;
//                syncPhoneDateToDevice();
//            }
//
//            @Override
//            public void onFailure(int code) {
//                if(reSendCnt < 10) {
//                    getDeviceInfo();
//                    reSendCnt++;
//                }
//            }
//        });
        // 同步结束后，调用
        listener.onInfoSync(1); // 数据同步完成后，回调1
    }

    private void syncPhoneDateToDevice() {
        Log.d(TAG, "syncPhoneDateToDevic: "  + " " + mOwnerDevice.mDeviceId);
//        final Cmd85SyncPhoneDate cmd = new Cmd85SyncPhoneDate();
//        Calendar cal = Calendar.getInstance();
//        cmd.setHour(cal.get(Calendar.HOUR_OF_DAY));
//        cmd.setMinute(cal.get(Calendar.MINUTE));
//        cmd.setSecond(cal.get(Calendar.SECOND));
//        cmd.setWeek(cal.get(Calendar.DAY_OF_WEEK)-1);
//        if(cmd.getWeek() == 0) {
//            cmd.setWeek(7);
//        }
//        DataSendTransferBle.writeBleDevice(cmd, mOwnerDevice.mDeviceId, new OnRespResultListener() {
//            @Override
//            public void onSuccess(int code) {
//                listener.onInfoSync(1); // 数据同步完成后，回调1
//                reSendCnt = 0;
//            }
//
//            @Override
//            public void onFailure(int code) {
//                if(reSendCnt < 10) {
//                    syncPhoneDateToDevice();
//                    reSendCnt++;
//                }
//            }
//        });
    }

}

