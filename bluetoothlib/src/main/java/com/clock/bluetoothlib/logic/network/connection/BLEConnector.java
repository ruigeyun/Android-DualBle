/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 */
public final class BLEConnector {

    private final String TAG = "BLEConnector";
    private Context mContext;
    private Random mRandom = new Random();
    private BLEServerListener mBLEServerListener;
    private final ArrayList<Integer> bleConnectingList = new ArrayList<>(); // 正在连接的设备

    private static BLEConnector instance = new BLEConnector();
    static BLEConnector getInstance() {
        return instance;
    }
    private BLEConnector() {}

    void init(Context context, BLEServerListener listener) {
        mContext = context;
        mBLEServerListener = listener;

        Log.w(TAG, "init: BLEConnecto");
    }

    /**
     * 连接虚拟设备
     */
    void connToVirtualBleDevice(BluetoothDevice bluetoothDevice) {
        connScannedBle(bluetoothDevice);
    }

    /**
     * 连接自动扫描的设备，一般是开机自动连接那些连接过的设备的过程，先显示，后连接
     */
    void connToAutoScanBleDevice(BluetoothDevice bluetoothDevice) {
        connScannedBle(bluetoothDevice);
        mBLEServerListener.onAddPreDevice(bluetoothDevice);
    }

    /**
     * 连接手动扫描到了的设备
     */
    int connToScannedBleDevice(BluetoothDevice bluetoothDevice) {
        return connScannedBle(bluetoothDevice);
    }

    void addConnectingDevice(Integer deviceId) {
        synchronized (bleConnectingList) {
            if (!bleConnectingList.contains(deviceId)) {
                bleConnectingList.add(deviceId);
                Log.w(TAG, "addConnectingDevic: " + deviceId);
            }
        }
    }
    void removeConnectingDevice(Integer deviceId) {
        synchronized (bleConnectingList) {
            bleConnectingList.remove(deviceId);
            Log.w(TAG, "removeConnectingDevic: " + deviceId);
        }
    }
    int getConnectingDeviceNum() {
        synchronized (bleConnectingList) {
            return bleConnectingList.size();
        }
    }

    private int connScannedBle(final BluetoothDevice bluetoothDevice) {

        if (getConnectingDeviceNum() >= 1) { // 有其他设备在连接，此处设定最多同时连1个
            Log.w(TAG, "想连接设备，有其他设备在连接，请等待： " + getConnectingDeviceNum());
            return -1;
        }

        int disConnDevice = BLEServerCentral.getInstance().getDisConnDeviceNum();
        if (disConnDevice >0) {
            Log.w(TAG, "想连接设备，但是有其他设备 还没释放完，请等待: " + disConnDevice);
            return -3;
        }

        connBle(bluetoothDevice);
        return 0;
    }

    void connBle(final BluetoothDevice bluetoothDevice) {
        int deviceType = -1;
        for(BLEScanDevice scanDevice : BLEScanner.getInstance().deviceScanList) {
            if (scanDevice.scanBle.equals(bluetoothDevice)) {
                deviceType = scanDevice.deviceType;
            }
        }
        if (deviceType == -1) {
            Log.w(TAG, "创建设备前，找不到设备类型");
            return;
        }

        BLEAppDevice bLEAppDevice = allocateOneBleDevice(bluetoothDevice, deviceType); // 根据类型创建设备
        bLEAppDevice.mDeviceTypeId = deviceType;

        bLEAppDevice.connBleSelf();
    }

    private BLEAppDevice allocateOneBleDevice(BluetoothDevice bluetoothDevice, int type) {
        for(BLELogicDevice value : BLEServerCentral.getInstance().deviceConnectedMap.values()){
            if(value.mBleAddress.equals(bluetoothDevice.getAddress())) {
                Log.w(TAG, "ble 连接列表 已经包含了这个设备： " + bluetoothDevice.getAddress());
                return null;
            }
        }

        int id = mRandom.nextInt(10000);
        Log.w(TAG, "connToBleDevic: 创建新设备分配 id：" + id);

        BLEAppDevice bleDevice = mBLEServerListener.onCreateDevice(bluetoothDevice, type);
        bleDevice.initBle(bluetoothDevice, id, mContext, mBLEServerListener, BLEServerCentral.getInstance().mDeviceStatusListener);
        return bleDevice;
    }

}
