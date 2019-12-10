/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.content.Context;
import android.util.Log;

import com.clock.bluetoothlib.logic.network.BleLibByteUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

final class BLEServerCentral {

    private final String TAG = "BLEServerCentral";
    private Context mContext;
    final ConcurrentHashMap<Integer, BLELogicDevice> deviceConnectedMap = new ConcurrentHashMap<>(); // 已经连接的设备列表，不一定连接成功
//    private final ArrayList<BLELogicDevice> bleScannedBeToConnList = new ArrayList<>(); // 准备去连接的设备
//    private final ArrayList<Integer> bleConnectingList = new ArrayList<>(); // 正在连接的设备
    private final ArrayList<BLELogicDevice> beToDisConnBleList = new ArrayList<>(); // 准备去断开连接的设备列表，排队依次断开

    private BLEServerListener mBLEServerListener;

    private Timer toBeDisConnTimer = new Timer();

//    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private final int BleSystemReleaseTime = 1500;
//    boolean enableConnect = true;

    private static BLEServerCentral instance = new BLEServerCentral();
    static BLEServerCentral getInstance() {
        return instance;
    }
    private BLEServerCentral() {}

//    BLEServerCentral(Context context, BLEServerListener listener) {
//        setServerListener(listener);
//        init(context);
//    }

    void init(Context context, BLEServerListener listener) {
        mContext = context;
        setServerListener(listener);
//        managerConnScannedBleNum();
        managerBleDisConn();
        Log.w(TAG, "init: BLEServerCentral");
    }

    private void setServerListener(BLEServerListener listener) {
        mBLEServerListener = listener;
    }

    private void managerBleDisConn() { // 系统蓝牙资源，等待1-2s的释放时间后，才能继续连接别的设备
        toBeDisConnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getDisConnDeviceNum() > 0) {
                    if (beToDisConnBleList.get(0).mBLEConnState == BLEState.Connect.ForceDisconnect) { // 下面停止的设备，释放掉，使能其他设备连接
                        removeDisConnDevice(beToDisConnBleList.get(0));
                    }
                }
                if (getDisConnDeviceNum() > 0) {
                    beToDisConnBleList.get(0).ForceDisConn(); // 执行后，状态变ForceDisconnect，1s后，检查其状态，然后移除
                }
            }
        }, 1000, BleSystemReleaseTime);
    }


    int manualConnBle(Integer key) {
        Log.w(TAG,"想手动重连设备 ble key: " + key);
        BLELogicDevice device = deviceConnectedMap.get(key);
        if (device == null) {
            Log.w(TAG, "想手动重连设备，但是不存在这个设备 " + " " + key);
            return -10;
        }

        int connectingDevice = BLEConnector.getInstance().getConnectingDeviceNum();
        if (connectingDevice > 0) {
            Log.w(TAG, "想手动重连设备，但是有其他设备在连，只能退出等待： " + connectingDevice + " " + key);
            return -1;
        }
        int disConnDevice = getDisConnDeviceNum();
        if (disConnDevice >0) {
            Log.w(TAG, "想手动重连设备，但是有其他设备 还没释放完，只能退出等待: " + disConnDevice + " " + key);
            return -3;
        }

        if (device.mDeviceState != DeviceState.Normal) {
            Log.w(TAG, "想手动重连设备，设备资源还没释放完，只能退出等待: " + " " + key);
            return -20;
        }

        return device.manualConnBle();
    }

    int forceDisConnBle(Integer key) {
        Log.w(TAG,"想手动断开设备 ble key: " + key);
        BLELogicDevice device = deviceConnectedMap.get(key);
        if (device == null) {
            Log.w(TAG, "想手动断开设备，但是不存在这个设备 " + " " + key);
            return -10;
        }

        int connectingDevice = BLEConnector.getInstance().getConnectingDeviceNum();
        if (connectingDevice > 0) {
            Log.w(TAG, "想手动断开设备，但是有其他设备在连，只能退出等待： " + connectingDevice + " " + key);
            return -1;
        }
        int disConnDevice = getDisConnDeviceNum();
        if (disConnDevice >0) {
            Log.w(TAG, "想手动断开设备，但是有其他设备 还没释放完，只能退出等待: " + disConnDevice + " " + key);
            return -3;
        }

        if (device.mBLEConnState != BLEState.Connect.ForceDisconnect) {
            Log.d(TAG, "强制断开此设备，加入断开列表：" + key);
            device.mBLEConnState = BLEState.Connect.BeToDisconnect;
            addDisConnDevice(device);
            return 0;
        }
        else {
            Log.d(TAG, "此设备处于断开状态，不必再断开"+ " " + key);
            return -20;
        }
    }

    void removeBle(Integer key) {
        Log.w(TAG,"remove ble key: " + key);
        BLELogicDevice device = deviceConnectedMap.get(key);
        if (device == null) {
            Log.e(TAG, "removeBl no this device");
            return;
        }

        device.removeSelf();
        deviceConnectedMap.remove(key);
        BLEConnector.getInstance().removeConnectingDevice(key);
        System.gc();
    }

//    void sendConfigData(byte[] data, int bleId) {
//        Log.e(TAG, bleId + " sendConfigData: " + BleLibByteUtil.BytesToHexStringPrintf(data));
//        BLELogicDevice d = deviceConnectedMap.get(bleId);
//        if(d==null /*|| d.mBLEConnState != BLEState.Active*/) {
//            Log.w(TAG, "sendConfigData null or not Active: ");
//            return;
//        }
//        d.writeConfigData(data);
//    }

    void sendTransmitData(byte[] data, int bleId) {
        Log.e(TAG, bleId + " sendTransmitData: " + BleLibByteUtil.BytesToHexStringPrintf(data));
        BLELogicDevice d = deviceConnectedMap.get(bleId);
        if(d==null) {
            Log.w(TAG, "sendTransmitData null ");
            mBLEServerListener.onDeviceSendResult("找不到设备 " + bleId);
            return;
        }
        if( d.mBLEConnState.index < BLEState.Connect.Enable.index) {
            Log.w(TAG, "sendTransmitData not Active: ");
            mBLEServerListener.onDeviceSendResult("蓝牙连接失败 " + bleId);
            return;
        }
        d.writeTransmitData(data);
    }

//    private void addConnectingDevice(Integer bleID) {
//        synchronized (bleConnectingList) {
//            if (!bleConnectingList.contains(bleID)) {
//                bleConnectingList.add(bleID);
//                Log.w(TAG, "addConnectingDevic: " + bleID);
//            }
//        }
//    }
//    private void removeConnectingDevice(Integer bleID) {
//        synchronized (bleConnectingList) {
//            bleConnectingList.remove(bleID);
//            Log.w(TAG, "removeConnectingDevic: " + bleID);
//        }
//    }
//    int getConnectingDeviceNum() {
//        synchronized (bleConnectingList) {
//            return bleConnectingList.size();
//        }
//    }

    private void addDisConnDevice(BLELogicDevice bleID) {
        synchronized (beToDisConnBleList) {
            if (!beToDisConnBleList.contains(bleID)) {
                beToDisConnBleList.add(bleID);
            }
        }
    }
    private void removeDisConnDevice(BLELogicDevice bleID) {
        synchronized (beToDisConnBleList) {
            beToDisConnBleList.remove(bleID);
        }
    }

    int getDisConnDeviceNum() {
        synchronized (beToDisConnBleList) {
            return beToDisConnBleList.size();
        }
    }

    /**
     * 独立设备跟服务中心的信息交互接口
     * 设备的各种状态
     */
    interface DeviceStatusListener {
        void onRemove(BLELogicDevice device);
        void onConnected(BLELogicDevice device, Integer bleID);
        void onConnecting(BLELogicDevice device, Integer bleID);
        void onConnectFail(BLELogicDevice device, Integer bleID);
    }

    DeviceStatusListener mDeviceStatusListener = new DeviceStatusListener() {
        @Override
        public void onRemove(BLELogicDevice device) {
            removeBle(device.mDeviceId);
        }
        @Override
        public void onConnected(BLELogicDevice device, Integer bleID) {
            BLEConnector.getInstance().removeConnectingDevice(bleID);
            Log.w(TAG, "connectingBleCn ed : " + BLEConnector.getInstance().getConnectingDeviceNum());
        }
        @Override
        public void onConnecting(BLELogicDevice device, Integer bleID) {
            if (!deviceConnectedMap.containsValue(device)) {
                // 设备连接成功，添加到集合
                deviceConnectedMap.put(bleID, device);
                Log.w(TAG, "连接设备集合增加一成员 : " + bleID);
            }
            else {
                Log.w(TAG, "设备集合已经存在它，不再反复添加 : " + bleID);
            }
            BLEConnector.getInstance().addConnectingDevice(bleID);
            Log.w(TAG, "connectingBleCn ing : " + BLEConnector.getInstance().getConnectingDeviceNum() + " " + bleID);
        }
        @Override
        public void onConnectFail(BLELogicDevice device, Integer bleID) {
            Log.w(TAG, "connect fail : " + BLEConnector.getInstance().getConnectingDeviceNum());
            BLEConnector.getInstance().removeConnectingDevice(bleID);
        }
    };

}
