/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.clock.bluetoothlib.logic.network.BleLibByteUtil;
import com.clock.bluetoothlib.logic.network.data.DataCircularBuffer;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

abstract class BLELogicDevice extends BLEBaseDevice {

    private final String TAG = "BLELedDevice";

    BluetoothDevice mBluetoothDevice;
//    BluetoothGattCallback mGattCallback;
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

    /**
     * 蓝牙设备的service uuid ，具体类必须实现，返回设备的service uuid
     * @return
     */
    public abstract UUID getServiceUUID();
    /**
     * 蓝牙设备的接收uuid ，具体类必须实现，返回设备的接收uuid
     * @return
     */
    public abstract UUID getRxUUID();
    /**
     * 蓝牙设备的写入uuid ，具体类必须实现，返回设备的写入uuid
     * @return
     */
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

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothGatt = mBluetoothDevice.connectGatt(mContext,
                            false, mGattCallback, TRANSPORT_LE);
                } else {
                    mBluetoothGatt = mBluetoothDevice.connectGatt(mContext,
                            false, mGattCallback);
                }
                if (mBluetoothGatt == null) {
                    LogUtil.e(TAG, "connectGatt 失败", (BLEAppDevice)BLELogicDevice.this);
                    mDeviceStatusListener.onConnectFail(BLELogicDevice.this, mDeviceId);

                } else {
                    if(connectTimer != null) {
                        LogUtil.w(TAG, "222 启动新的连接，老的连接定时器 cancel", (BLEAppDevice)BLELogicDevice.this);
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
        });

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

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (mGattCallback == null || mDeviceState == DeviceState.Remove) {
                // 设备被删除后，还有回调
                LogUtil.w(TAG, "设备被删除了，不再处理回调");
                return;
            }
            if (mBLEConnState == BLEState.Connect.ForceDisconnect) {
                LogUtil.w(TAG, "ble状态改变，因为设备已被强制断开，直接清空: ", (BLEAppDevice) BLELogicDevice.this);
                closeBleRelation(gatt);
                return;
            }

            mBluetoothGatt = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                LogUtil.w(TAG, "设备连接上：", (BLEAppDevice) BLELogicDevice.this);
//                LogUtil.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    requestMasterConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH, mBluetoothGatt);
                }
                LogUtil.d(TAG, "延时500ms后，去发现服务", (BLEAppDevice) BLELogicDevice.this);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean ret = mBluetoothGatt.discoverServices();
                        LogUtil.w(TAG, "发现服务ret: ", (BLEAppDevice) BLELogicDevice.this);
                        if (ret) {
                            mBLEConnState = BLEState.Connect.Connected;
                            mBLELedBiz.doConnectedAction();
                        }
                        else {
                            LogUtil.e(TAG, "发现服务失败", (BLEAppDevice) BLELogicDevice.this);
                            disconnectGatt();
                            refreshDeviceCache();
                            closeBluetoothGatt();
                            mBLEServerListener.onCharacterMatch(mBluetoothDevice, 0);
                            mDeviceStatusListener.onRemove(BLELogicDevice.this);
                        }
                    }
                }, 500);

            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                LogUtil.w(TAG, "Disconnected from GATT server.", (BLEAppDevice) BLELogicDevice.this);
                if (mReconnectType == BLEState.ReconnectType.Auto) {
                    mBLELedBiz.doDisconnectAction(); // 自动重连情况，才去做
                }
                else {
                    connectFailAction();
                }
            }
            if(status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.w(TAG, "BluetoothGatt.GATT_SUCCESS.", (BLEAppDevice) BLELogicDevice.this);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtil.w(TAG, "发现服务回调 onServicesDiscovered status : ", (BLEAppDevice) BLELogicDevice.this);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                LogUtil.w(TAG, "发现服务 还没成功 ", (BLEAppDevice) BLELogicDevice.this);
                return;
            }

            mBluetoothGatt = gatt;
            for (BluetoothGattService gattService : gatt.getServices()) {
//                LogUtil.i(TAG, "discover: " + gattService.toString());
                LogUtil.i(TAG, "discover 服务的uuid : " + gattService.getUuid(), (BLEAppDevice) BLELogicDevice.this);
                if (!getServiceUUID().equals(gattService.getUuid())) {
                    continue;
                }
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                    LogUtil.d(TAG, "特征的UUID: " + gattCharacteristic.getUuid().toString(), (BLEAppDevice) BLELogicDevice.this);
                    if (gattCharacteristic.getUuid().equals(getRxUUID())) {
                        rxGattCharacteristic = gattCharacteristic;
                        LogUtil.w(TAG, "gattCharacteristics-----rx: " + gattCharacteristic.getUuid().toString(), (BLEAppDevice) BLELogicDevice.this);
                        // getPermissions: 0
                        LogUtil.w(TAG, "rx getPermissions: " + gattCharacteristic.getPermissions(), (BLEAppDevice) BLELogicDevice.this);
                        // getProperties: 18 (BluetoothGattCharacteristic.PROPERTY_NOTIFY | PROPERTY_READ)
                        LogUtil.w(TAG, "rx getProperties: " + gattCharacteristic.getProperties(), (BLEAppDevice) BLELogicDevice.this);
                    }
                    else if (gattCharacteristic.getUuid().equals(getTxUUID())) {
                        txGattCharacteristic = gattCharacteristic;
                        LogUtil.w(TAG, "gattCharacteristics-----tx: " + gattCharacteristic.getUuid().toString(), (BLEAppDevice) BLELogicDevice.this);
                        // getPermissions: 0
                        LogUtil.w(TAG, "TX getPermissions: " + gattCharacteristic.getPermissions(), (BLEAppDevice) BLELogicDevice.this);
                        // getProperties: 4 (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                        LogUtil.w(TAG, "TX getProperties: " + gattCharacteristic.getProperties(), (BLEAppDevice) BLELogicDevice.this);
                    }
                }
            }

            if ((rxGattCharacteristic != null) && (txGattCharacteristic != null)) {

                mBLEServerListener.onCharacterMatch(mBluetoothDevice, 1);
                LogUtil.d(TAG, "延时200ms后，开始设置特征", (BLEAppDevice) BLELogicDevice.this);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (enableNotifyCharacter()) {
                            if(connectTimer != null) {
                                LogUtil.w(TAG, "15秒未到，连接成功 connectTimer cancel", (BLEAppDevice) BLELogicDevice.this);
                                connectTimer.cancel();
                            }
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mDeviceStatusListener.onConnected(BLELogicDevice.this, mDeviceId);
                                    mBLELedBiz.doCharacterEnableAction();
                                }
                            }, 200);
                        }
                        else {
                            LogUtil.e(TAG, "设置 配置 特性 失败", (BLEAppDevice) BLELogicDevice.this);
                            mBLEServerListener.onCharacterMatch(mBluetoothDevice, 0);
                            mDeviceStatusListener.onRemove(BLELogicDevice.this);
                        }
                    }
                }, 200);
            }
            else {
                LogUtil.e(TAG, "发现服务中，没有对应的uuid，无法连接该设备，删除", (BLEAppDevice) BLELogicDevice.this);
                mBLEServerListener.onCharacterMatch(mBluetoothDevice, 0);
                mDeviceStatusListener.onRemove(BLELogicDevice.this);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            LogUtil.d(TAG, "onDescriptorWrite: " + status, (BLEAppDevice) BLELogicDevice.this);
            LogUtil.w(TAG, "onDescriptorWrite : " + BleLibByteUtil.BytesToHexStringPrintf(descriptor.getValue()));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            LogUtil.d(TAG, "onCharacteristicRead : " + characteristic.getUuid() + " status: " + status);
            LogUtil.w(TAG, "onCharacteristicRead : " + BleLibByteUtil.BytesToHexStringPrintf(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            LogUtil.w(TAG, "onCharacteristicChanged: " + characteristic.getUuid() + " WriteType : " + characteristic.getWriteType());
            LogUtil.w(TAG, "onCharacteristicChanged value: " + BleLibByteUtil.BytesToHexStringPrintf(characteristic.getValue()));
            if(characteristic == rxGattCharacteristic) {
                mCircularBuffer.pushOriginalDataToBuffer(characteristic.getValue());
            }
//            else if (characteristic == settingGattCharacteristic) {
//                mBLELedBiz.doConfigRespData(characteristic.getValue());
//            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.v(TAG, "onCharacteristicWrite: " + characteristic.getUuid() + " status: " + status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                boolean ret  = mBluetoothGatt.readCharacteristic(rxGattCharacteristic);
//                LogUtil.w(TAG, "readCharacteristic ret : " + ret);
//            }

        }
    };

    @Override
    protected void finalize() throws Throwable {
        LogUtil.w(TAG, "device destroy..");
        super.finalize();
    }
}

