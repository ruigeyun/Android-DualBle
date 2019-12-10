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

    public void doConfiguredAction() {
        getDeviceInfo();
    }

//    private void sendPwdToBleDevice() {
//        Log.d(TAG, "密码验证: "  + " " + mOwnerDevice.mDeviceId);
//        Cmd71Pwd cmd = new Cmd71Pwd();
//
//        DataSendTransferBle.writeBleDevice(cmd, mOwnerDevice.mDeviceId, new OnRespResultListener() {
//            @Override
//            public void onSuccess(int code) {
//                Log.i("test", "--led 蓝牙-密码验证成功--=");
////                mOwnerDevice.updateConnectState(BLEState.Active);
//                reSendCnt = 0;
//                getDeviceInfo();
//            }
//
//            @Override
//            public void onFailure(int code) {
//                Log.i("test", "--led 蓝牙-密码验证失败--=");
////                getDeviceInfo();
//                if(reSendCnt < 10) {
//                    sendPwdToBleDevice();
//                    reSendCnt++;
//                }
//            }
//        });
//    }

    private void getDeviceInfo() {
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
//                listener.onInfoSync(1);
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

