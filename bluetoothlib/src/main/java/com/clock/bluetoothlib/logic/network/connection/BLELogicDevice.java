/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.clock.bluetoothlib.logic.network.BleLibByteUtil;
import com.clock.bluetoothlib.logic.network.data.DataCircularBuffer;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

abstract class BLELogicDevice extends BLEBaseDevice {

    private final String TAG = "BLELedDevice";

    BluetoothDevice mBluetoothDevice;
    BluetoothGattCallback mGattCallback;
//    BluetoothGattCharacteristic settingGattCharacteristic;
    BluetoothGattCharacteristic rxGattCharacteristic;//
    BluetoothGattCharacteristic txGattCharacteristic;//

    private Context mContext;
    BLEDeviceBiz mBLELedBiz;
    DataCircularBuffer mCircularBuffer;
    private DataParserAdapter adapter;

    BLEServerListener mBLEServerListener;
    BLEServerCentral.DeviceStatusListener mDeviceStatusListener;
    Timer connectTimer;
    boolean isDeviceFirstAdd = true; // 第一次添加设备到显示列表，后续的重连，不添加

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int ConnectTimeOut = 10000; // 连接超时，提示连接失败
    private int ConnectFailReleaseTime = 2300; // 连接失败，设备资源释放时间
    private int DisConnReleaseTime = 2000; // 断开它，马上又连它的间隔时间，加上前面有BleSystemReleaseTime 时间共2-3.5秒

    BLELogicDevice() {}

    BLELogicDevice(DataParserAdapter adapter) {
        this.adapter = adapter;
    }

    void initBle(BluetoothDevice device, int customId, Context context, BLEServerListener listener, BLEServerCentral.DeviceStatusListener Listener2) {
        mBluetoothDevice = device;
        mDeviceId = customId;
        mBleAddress = device.getAddress();
        mBleName = device.getName();
        mContext = context;
        mBLEServerListener = listener;
        mCircularBuffer = new DataCircularBuffer(256, mDeviceId, adapter);
        mBLELedBiz = new BLEDeviceBiz(this);
        this.mDeviceStatusListener = Listener2;
    }

    public abstract UUID getServiceUUID();
    public abstract UUID getRxUUID();
    public abstract UUID getTxUUID();

    @Override
    void connBleSelf() {
        if (mBLEConnState == BLEState.Connect.ForceDisconnect) { // 防止自动重连的情况
            LogUtil.w(TAG, "设备已被强制断开，不去连接: ", (BLEAppDevice) this);
            clearBleConnResource();
            return;
        }
        LogUtil.e(TAG, "connBl: 222 真正去连接设备: ", (BLEAppDevice) this);
        if (mBLEConnState != BLEState.Connect.Connecting) {
            // 正在连接的信息，只发一次，后面可能有反复重连
            mDeviceStatusListener.onConnecting(BLELogicDevice.this, mDeviceId);
        }
        mBLEConnState = BLEState.Connect.Connecting;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext,
                    false, mGattCallback, TRANSPORT_LE);
        } else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext,
                    false, mGattCallback);
        }
        if (mBluetoothGatt == null) {
            LogUtil.e(TAG, "connectGatt 失败", (BLEAppDevice) this);
            mDeviceStatusListener.onConnectFail(BLELogicDevice.this, mDeviceId);

        } else {
            if(connectTimer != null) {
                LogUtil.w(TAG, "222 启动新的连接，老的连接定时器 cancel", (BLEAppDevice) this);
                connectTimer.cancel();
            }
            connectTimer = new Timer();
            connectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LogUtil.e(TAG, ConnectTimeOut + " 秒后，还没连接上，自动断开，连接失败2222 !", (BLEAppDevice) BLELogicDevice.this);
                    connectFailAction();
                }
            }, ConnectTimeOut);
        }
    }
    @Override
    synchronized int manualConnBle() {
//        LogUtil.w(TAG, "reConnBl: manual重接设备: " + mDeviceId);
        if (mBLEConnState.index >= BLEState.Connect.Connecting.index) {
            LogUtil.w(TAG, "想手动重连设备，设备状态不允许！", (BLEAppDevice) this);
            return -2;
        }
        mReconnectType = BLEState.ReconnectType.Manual;
        mBLEConnState = BLEState.Connect.InitiativeConnect;
        connBleSelf();
        return 0;
    }
    @Override
    void ForceDisConn() {
        LogUtil.w(TAG, "Force DisConn id: ", (BLEAppDevice) this);
        mBLEConnState = BLEState.Connect.ForceDisconnect;
        releaseBleSelf(DisConnReleaseTime);
    }
    @Override
    void removeSelf() {
        LogUtil.w(TAG, "removeSel id: ", (BLEAppDevice) this);
        mHandler.removeCallbacksAndMessages(null);
        mDeviceState = DeviceState.Remove;
        clearBleConnResource();
        clearMemoryResources();
        mBLELedBiz.clearResource();
    }
    @Override
    void clearBleConnResource() {
        LogUtil.w(TAG, "clear Ble Relation: ", (BLEAppDevice) this);
        disconnectGatt();
        refreshDeviceCache();
        closeBluetoothGatt();
    }

    void closeBleRelation(BluetoothGatt gatt) {
        if (gatt != null) {
            LogUtil.w(TAG, "close Ble Relation", (BLEAppDevice) this);
            mBluetoothGatt = gatt;
            clearBleConnResource();
        }
    }
    private synchronized void disconnectGatt() {
        if (mBluetoothGatt != null) {
            LogUtil.w(TAG, "gatt disconnectGat", (BLEAppDevice) this);
            mBluetoothGatt.disconnect();
        }
    }

    private synchronized void refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mBluetoothGatt != null) {
                boolean success = (Boolean) refresh.invoke(mBluetoothGatt);
                LogUtil.w(TAG,"refreshDeviceCache, is success:  " + success, (BLEAppDevice) this);
            }
        } catch (Exception e) {
            LogUtil.e(TAG,"exception occur while refreshing device: " + e.getMessage(), (BLEAppDevice) this);
            e.printStackTrace();
        }
    }

    private synchronized void closeBluetoothGatt() {
        if (mBluetoothGatt != null) {
            LogUtil.w(TAG, "close Bluetooth Gatt", (BLEAppDevice) this);
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * 把自身的连接状态释放掉，释放一段时间后，才能被再次连接
     */
    private void releaseBleSelf(int time) {
        LogUtil.w(TAG, "Releasing Ble Resource", (BLEAppDevice) this);
        mDeviceState = DeviceState.DisConnRelease;
        clearBleConnResource();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDeviceState = DeviceState.Normal;
            }
        }, time);
    }

    @Override
    void clearMemoryResources() {
        if(connectTimer != null) {
            connectTimer.cancel();
            connectTimer = null;
        }
        mGattCallback = null;
        mCircularBuffer.destroyBuffer();
        mContext = null;
        mBleName = null;
        mReconnectType = BLEState.ReconnectType.Manual;
        mBluetoothDevice = null;
        mDeviceId = 0;
        mBleAddress = "";
        mBLEConnState = BLEState.Connect.ConnectIdle;
        mDeviceState = DeviceState.Normal;
        isDeviceFirstAdd = true;
    }
    @Override
    synchronized void writeConfigData(byte[] data) {
//        LogUtil.i(TAG, mBleAddress + " writeConfigData: " + BytesUtil.BytesToHexStringPrintf(data));
////        settingGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//        settingGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//        settingGattCharacteristic.setValue(data);
//
//        if(mBluetoothGatt != null) {
//            mBluetoothGatt.writeCharacteristic(settingGattCharacteristic);
//        }
    }
    @Override
    synchronized void writeTransmitData(byte[] data) {
//        LogUtil.e(TAG, "writeTransmitData: begin..");
        if(txGattCharacteristic == null) {
            LogUtil.w(TAG, "txGattCharacteristi: null 还没准备好，就发数据了", (BLEAppDevice) BLELogicDevice.this);
            mBLEServerListener.onDeviceSendResult("蓝牙设备还没准备好");
            return;
        }
        LogUtil.i(TAG, mBleAddress + " writeTransmitData: " + BleLibByteUtil.BytesToHexStringPrintf(data), (BLEAppDevice) BLELogicDevice.this);
        txGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//        txGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        txGattCharacteristic.setValue(data);

        if(mBluetoothGatt != null) {
            boolean ret = mBluetoothGatt.writeCharacteristic(txGattCharacteristic);
            LogUtil.d(TAG, "writeCharacteristic ret : " + ret, (BLEAppDevice) BLELogicDevice.this);
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        LogUtil.e(TAG, "writeTransmitData: over");
    }

    @Override
    boolean enableNotifyCharacter() {
        LogUtil.w(TAG, "配置特征通讯方式: ", (BLEAppDevice) this);
//        return setCharacteristicIndication(rxGattCharacteristic, true);
        return setCharacteristicNotification(rxGattCharacteristic, true);
    }

    @Override
    public void updateDeviceInfo(Object info) {

        mBLEServerListener.onUpdateDeviceInfo((BLEAppDevice) this);
    }

    void connectFailAction() {
        LogUtil.v(TAG, "do connectFail Action", (BLEAppDevice) this);
        mBLELedBiz.clearTimer();

        mBLEConnState = BLEState.Connect.ConnectFail;
        releaseBleSelf(ConnectFailReleaseTime);

        mDeviceStatusListener.onConnectFail(BLELogicDevice.this, mDeviceId);
        mBLEServerListener.onConnectDevice((BLEAppDevice) BLELogicDevice.this, 0);
    }


    @Override
    protected void finalize() throws Throwable {
        LogUtil.w(TAG, "device destroy..");
        super.finalize();
    }
}

