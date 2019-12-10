/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

final class BLEScanner implements BluetoothAdapter.LeScanCallback {

    private final String TAG = "BLEScanner";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private UUID SERVICE_UUID;

    BLEState.Scan mBLEScanState = BLEState.Scan.ScanIdle;
//    long scanStartTime = 0;
    private ArrayList<BluetoothDevice> deviceScanList = new ArrayList<>();// 扫描到的蓝牙设备

    private BLEServerListener mListener;

//    private static BLEScanner instance = new BLEScanner();
//    public static BLEScanner getInstance() {
//        return instance;
//    }
//    private BLEScanner() {}

    BLEScanner(Context context, HashMap<Integer, UUID> serverUuidMap, boolean isScanFilter, BLEServerListener listener) {
        if (serverUuidMap!=null && serverUuidMap.keySet().size()==1 && isScanFilter) {
            LogUtil.d(TAG, "serviceUuid: " + serverUuidMap.values().iterator().next());
            init(context, serverUuidMap.values().iterator().next(), listener);
        }
        else {
            init(context, null, listener);
        }

    }

    private void init(Context context, UUID uuid, BLEServerListener listener) {
        SERVICE_UUID = uuid;
        mListener = listener;

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        else {
            Log.w(TAG, "init: bluetoothManager null");
        }

        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }

        Log.w(TAG, "init: BLEServerCentral");
    }

    boolean isBleEnable() {
        return bluetoothAdapter.isEnabled();
    }

    boolean scanBLE() {

        deviceScanList.clear();
        if(mBLEScanState == BLEState.Scan.Scanning) {
            Log.d(TAG, "scan: is scanning just waiting");
            return false;
        }
//        scanStartTime = System.currentTimeMillis();
        Log.d(TAG, "scan: start scan my ");
        mBLEScanState = BLEState.Scan.Scanning;

        UUID[] serviceUuids = new UUID[]{SERVICE_UUID};
//        UUID[] serviceUuids = new UUID[]{BLELedDevice.SERVICE_UUID, UUID_Service_MH}; // 不能这样，这是全部包含，才扫描到，是与的关系，不是或
        if (SERVICE_UUID == null) {
            return bluetoothAdapter.startLeScan(this);
        }
        else {
            return bluetoothAdapter.startLeScan(serviceUuids, this);
        }
    }

    void stopBleScan() {
        Log.d(TAG, "stopBleSca... ");
        if(mBLEScanState != BLEState.Scan.Scanned) {
            bluetoothAdapter.stopLeScan(this);
            mBLEScanState = BLEState.Scan.Scanned;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecordBytes) {

        if (deviceScanList.contains(bluetoothDevice)) {
//            Log.d(TAG, "onLeScan: 已经包含这设备了");
            return;
        }
        Log.v(TAG, "onLeScan device: " + bluetoothDevice.getAddress() + " "
                + bluetoothDevice.getName() + " " + bluetoothDevice.getBondState() + " 信号强度：" + rssi
                + " " + bluetoothDevice.getUuids()); // uuid 为本地缓存的UUID，通常为null
        Log.w(TAG, "onLeScan: 把设备加入到扫描列表 " + bluetoothDevice.toString());
        deviceScanList.add(bluetoothDevice);

        mListener.onScannedDevice(bluetoothDevice);
        mListener.onAddScanDevice(bluetoothDevice);
    }

}
