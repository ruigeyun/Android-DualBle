/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.annotation.TargetApi;
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
import android.util.Log;

import com.clock.bluetoothlib.logic.network.BleLibByteUtil;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * 连接器，ble连接成功，发觉服务成功后，才能读到它的服务uuid，才能区分它属于哪一种设备
 *
 */
public final class BLEConnector {

    private final String TAG = "BLEConnector";
    private Context mContext;
    private Random mRandom = new Random();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private BLEServerListener mBLEServerListener;

    private final ArrayList<BluetoothDevice> bleScannedBeToConnList = new ArrayList<>(); // 准备去连接的设备
    private final ArrayList<Integer> bleConnectingList = new ArrayList<>(); // 正在连接的设备

    private HashMap<Integer, UUID> deviceServiceUUIDMap;

    private Timer toBeConnectTimer = new Timer();
    private Timer unTypeDeviceConnectTimer;
    private int ConnectTimeOut = 10000; // 连接超时，提示连接失败

    private static BLEConnector instance = new BLEConnector();
    static BLEConnector getInstance() {
        return instance;
    }
    private BLEConnector() {}

    void init(Context context, HashMap<Integer, UUID> serverUuid, BLEServerListener listener) {
        mContext = context;
        deviceServiceUUIDMap = serverUuid;
        mBLEServerListener = listener;

        managerConnScannedBleNum();
        Log.w(TAG, "init: BLEConnecto");
    }

    /**
     * 连接虚拟设备
     */
    void connToVirtualBleDevice(BluetoothDevice bluetoothDevice) {
        bleScannedBeToConnList.add(bluetoothDevice);
    }

    /**
     * 连接自动扫描的设备，一般是开机自动连接那些连接过的设备的过程，先显示，后连接
     */
    void connToAutoScanBleDevice(BluetoothDevice bluetoothDevice) {
        mBLEServerListener.onAddPreDevice(bluetoothDevice);
        bleScannedBeToConnList.add(bluetoothDevice);
    }

    /**
     * 连接手动扫描到了的设备
     */
    void connToScannedBleDevice(BluetoothDevice bluetoothDevice) {
        bleScannedBeToConnList.add(bluetoothDevice);
    }

    void addConnectingDevice(Integer bleID) {
        synchronized (bleConnectingList) {
            if (!bleConnectingList.contains(bleID)) {
                bleConnectingList.add(bleID);
                Log.w(TAG, "addConnectingDevic: " + bleID);
            }
        }
    }
    void removeConnectingDevice(Integer bleID) {
        synchronized (bleConnectingList) {
            bleConnectingList.remove(bleID);
            Log.w(TAG, "removeConnectingDevic: " + bleID);
        }
    }
    int getConnectingDeviceNum() {
        synchronized (bleConnectingList) {
            return bleConnectingList.size();
        }
    }

    private void managerConnScannedBleNum() {
        toBeConnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (bleScannedBeToConnList.size() <= 0) {
                    return;
                }

                if (getConnectingDeviceNum() >= 1) { // 有其他设备在连接，此处设定最多同时连1个
                    Log.w(TAG, "想连接设备，有其他设备在连接，请等待： " + getConnectingDeviceNum());
                    return;
                }

                int disConnDevice = BLEServerCentral.getInstance().getDisConnDeviceNum();
                if (disConnDevice >0) {
                    Log.w(TAG, "想连接设备，但是有其他设备 还没释放完，请等待: " + disConnDevice);
                    return ;
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        connUnTypeBle(bleScannedBeToConnList.get(0));
                        bleScannedBeToConnList.remove(0);
                    }
                });
            }
        }, 1000, 500);
    }

    /**
     * 连接未知类型的ble，还没连成过，无法知道是哪一种设备，手环？手表？
     */
    void connUnTypeBle(final BluetoothDevice bluetoothDevice) {

        GattCallback gattCallback = new GattCallback(bluetoothDevice);
        BluetoothGatt bluetoothGatt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = bluetoothDevice.connectGatt(mContext,
                    false, gattCallback, TRANSPORT_LE);
        } else {
            bluetoothGatt = bluetoothDevice.connectGatt(mContext,
                    false, gattCallback);
        }
        if (bluetoothGatt == null) {
            LogUtil.e(TAG, "connectGatt 失败"+ bluetoothDevice.getAddress());

            mBLEServerListener.onConnectUnTypeDevice(bluetoothDevice, -1);

        } else {
            if(unTypeDeviceConnectTimer != null) {
                LogUtil.w(TAG, "000 启动新的连接，老的连接定时器 cancel" + bluetoothDevice.getAddress());
                unTypeDeviceConnectTimer.cancel();
            }
            unTypeDeviceConnectTimer = new Timer();
            unTypeDeviceConnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LogUtil.e(TAG, ConnectTimeOut + " 秒后，还没连接上，自动断开，连接失败 000!" + bluetoothDevice.getAddress());
                    mBLEServerListener.onConnectUnTypeDevice(bluetoothDevice, -2);
                }
            }, ConnectTimeOut);
        }
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

    private synchronized void disconnectGatt(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    private synchronized void refreshDeviceCache(BluetoothGatt bluetoothGatt) {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && bluetoothGatt != null) {
                boolean success = (Boolean) refresh.invoke(bluetoothGatt);
                LogUtil.i(TAG, "refreshDeviceCache, is success:  " + success);
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "exception occur while refreshing device: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void closeBluetoothGatt(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }

    /**
     * 每一个设备，都会new 一个此对象，关联起来，这是APP与蓝牙设备信息交互的地方。
     * 蓝牙设备，必须在连接成功，发觉服务成功后，才能读到它的serviceUUID，才能确定它的设备类型
     * 里面包含两种处理：
     * 1、设备类型还没确定，只能拿BluetoothDevice，作为操作对象
     * 2、设备类型已经确定，mBLEAppDevice已经创建好，具体的设备作为操作对象
     */
    class GattCallback extends BluetoothGattCallback {

        private BLEAppDevice mBLEAppDevice = null;
        private BluetoothDevice mBluetoothDevice;
        private BluetoothGatt mBluetoothGatt;

        GattCallback(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (mBLEAppDevice != null) {
                if (mBLEAppDevice.mGattCallback == null || mBLEAppDevice.mDeviceState == DeviceState.Remove) {
                    // 设备被删除后，还有回调
                    LogUtil.w(TAG, "设备被删除了，不再处理回调");
                    return;
                }
                if (mBLEAppDevice.mBLEConnState == BLEState.Connect.ForceDisconnect) {
                    LogUtil.w(TAG, "ble状态改变，因为设备已被强制断开，直接清空: ", mBLEAppDevice);
                    mBLEAppDevice.closeBleRelation(gatt);
                    return;
                }
            }

            mBluetoothGatt = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                LogUtil.w(TAG, "设备连接上：" + gatt.getDevice().getAddress());
                if(mBLEAppDevice == null && unTypeDeviceConnectTimer != null) { // 未知类型设备连接成功，取消连接定时器
                    LogUtil.w(TAG, "未知设备连接定时器cancel" + gatt.getDevice().getAddress());
                    unTypeDeviceConnectTimer.cancel();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    requestMasterConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH, mBluetoothGatt);
                }
                LogUtil.d(TAG, "延时500ms后，去发觉服务" + gatt.getDevice().getAddress());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean ret = mBluetoothGatt.discoverServices();
                        LogUtil.w(TAG, "发觉服务ret: " + mBluetoothGatt.getDevice().getAddress());
                        if (ret) {
                            if (mBLEAppDevice != null) {
                                mBLEAppDevice.mBLEConnState = BLEState.Connect.Connected;
                                mBLEAppDevice.mBLELedBiz.doConnectedAction();
                            }
                        }
                        else {
                            LogUtil.e(TAG, "发觉服务失败" + mBluetoothGatt.getDevice().getAddress());
                            disconnectGatt(mBluetoothGatt);
                            refreshDeviceCache(mBluetoothGatt);
                            closeBluetoothGatt(mBluetoothGatt);
                            mBLEServerListener.onCharacterMatch(mBluetoothDevice, 0);
                            if (mBLEAppDevice != null) {
                                mBLEAppDevice.connectFailAction();
                            }
                        }
                    }
                }, 500);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                LogUtil.w(TAG, "Disconnected from GATT server." + gatt.getDevice().getAddress());
                if (mBLEAppDevice != null) {
                    if (mBLEAppDevice.mReconnectType == BLEState.ReconnectType.Auto) {
                        mBLEAppDevice.mBLELedBiz.doDisconnectAction(); // 自动重连情况，才去做
                    } else {
                        mBLEAppDevice.connectFailAction();
                    }
                }
                else {
                    mBLEServerListener.onConnectUnTypeDevice(mBluetoothDevice, -3);
                }
            }
            if(status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.w(TAG, "BluetoothGatt.GATT_SUCCESS." + gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtil.w(TAG, "发现服务回调 onServicesDiscovered status : " + gatt.getDevice().getAddress());
            if (status != BluetoothGatt.GATT_SUCCESS) {
                LogUtil.w(TAG, "发现服务 还没成功 " + gatt.getDevice().getAddress());
                return;
            }

            mBluetoothGatt = gatt;
            int deviceType = -1; // 根据serviceUUID，确定是哪一种设备
            for (BluetoothGattService gattService : gatt.getServices()) {
                LogUtil.i(TAG, "discover 服务的uuid : " + gattService.getUuid());
//                LogUtil.i(TAG, "discover 服务的uuid : " + gattService.getUuid() + " " + gatt.getDevice().getAddress());

                for(Integer key : deviceServiceUUIDMap.keySet()){
                    LogUtil.d(TAG, "搜索 service uuid key: " + key);
                    if (deviceServiceUUIDMap.get(key).equals(gattService.getUuid())) {
                        deviceType = key;
                        break;
                    }
                }
                if (deviceType == -1) {
                    continue;
                }

                if (mBLEAppDevice == null) { // 第一次连接，才需要创建设备，后面重连不再需要
                    mBLEAppDevice = allocateOneBleDevice(mBluetoothDevice, deviceType); // 根据类型创建设备
                    mBLEAppDevice.mGattCallback = GattCallback.this;
                    mBLEAppDevice.mDeviceTypeId = deviceType;
                    // 把创建的设备，添加到设备列表
                    BLEServerCentral.getInstance().deviceConnectedMap.put(mBLEAppDevice.mDeviceId, mBLEAppDevice);
                }
                else {
                    LogUtil.i(TAG, "设备已经创建过，重连就可以了");
                }
                mBLEAppDevice.mBluetoothGatt = mBluetoothGatt; // 重连前 gatt 已经被清空，连接后需重新赋值

                deviceType = -1;

                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                    LogUtil.d(TAG, "特征的UUID: " + gattCharacteristic.getUuid().toString(), (BLEAppDevice) mBLEAppDevice);
                    if (gattCharacteristic.getUuid().equals(mBLEAppDevice.getRxUUID())) {
                        mBLEAppDevice.rxGattCharacteristic = gattCharacteristic;
                        LogUtil.w(TAG, "gattCharacteristics-----rx: " + gattCharacteristic.getUuid().toString(), mBLEAppDevice);
                        // getPermissions: 0
                        LogUtil.w(TAG, "rx getPermissions: " + gattCharacteristic.getPermissions(), mBLEAppDevice);
                        // getProperties: 18 (BluetoothGattCharacteristic.PROPERTY_NOTIFY | PROPERTY_READ)
                        LogUtil.w(TAG, "rx getProperties: " + gattCharacteristic.getProperties(), mBLEAppDevice);
                    }
                    else if (gattCharacteristic.getUuid().equals(mBLEAppDevice.getTxUUID())) {
                        mBLEAppDevice.txGattCharacteristic = gattCharacteristic;
                        LogUtil.w(TAG, "gattCharacteristics-----tx: " + gattCharacteristic.getUuid().toString(), mBLEAppDevice);
                        // getPermissions: 0
                        LogUtil.w(TAG, "TX getPermissions: " + gattCharacteristic.getPermissions(), mBLEAppDevice);
                        // getProperties: 4 (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                        LogUtil.w(TAG, "TX getProperties: " + gattCharacteristic.getProperties(), mBLEAppDevice);
                    }
                }
            }

            if (mBLEAppDevice == null) {
                LogUtil.e(TAG, "未知类型设备，服务中，没有建立设备的uuid，无法识别");
                mBLEServerListener.onConnectUnTypeDevice(mBluetoothDevice, -3);
                return;
            }

            if ((mBLEAppDevice.rxGattCharacteristic != null) && (mBLEAppDevice.txGattCharacteristic != null)) {

                mBLEServerListener.onCharacterMatch(mBluetoothDevice, 1);
                LogUtil.d(TAG, "延时200ms后，开始设置特征", mBLEAppDevice);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBLEAppDevice.enableNotifyCharacter()) {
                            LogUtil.d(TAG, "设置 配置 特性 成功", mBLEAppDevice);
                            if(mBLEAppDevice.connectTimer != null) {
                                LogUtil.w(TAG, "15秒未到，连接成功 connectTimer cancel", mBLEAppDevice);
                                mBLEAppDevice.connectTimer.cancel();
                            }
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtil.d(TAG, "延时200ms了，进行设备与APP密码校验", mBLEAppDevice);
                                    mBLEAppDevice.mDeviceStatusListener.onConnected(mBLEAppDevice, mBLEAppDevice.mDeviceId);
                                    mBLEAppDevice.mBLELedBiz.doCharacterEnableAction();
                                }
                            }, 200);
                        }
                        else {
                            LogUtil.e(TAG, "设置 配置 特性 失败", mBLEAppDevice);
                            mBLEServerListener.onCharacterMatch(mBluetoothDevice, 0);
                            mBLEAppDevice.mDeviceStatusListener.onRemove(mBLEAppDevice);
                        }
                    }
                }, 200);
            }
            else {
                LogUtil.e(TAG, "发现服务中，没有对应的uuid，无法连接该设备，删除222", mBLEAppDevice);
                mBLEServerListener.onCharacterMatch(mBluetoothDevice, 0);
                mBLEAppDevice.mDeviceStatusListener.onRemove(mBLEAppDevice);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            LogUtil.d(TAG, "onDescriptorWrite: " + status, mBLEAppDevice);
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
            if(characteristic == mBLEAppDevice.rxGattCharacteristic) {
                mBLEAppDevice.mCircularBuffer.pushOriginalDataToBuffer(characteristic.getValue());
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestMasterConnectionPriority(int priority, BluetoothGatt gatt) {
//        if (gatt != null) {
//            boolean ret = gatt.requestConnectionPriority(priority);
//            LogUtil.w(TAG, " ret : " + ret);
//            //            boolean setMtu = gatt.requestMtu(84);
//            //            LogUtil.d("wsh", "蓝牙最大传输空间 ： " + setMtu);
//        } else {
//            LogUtil.w(TAG, " requestMasterConnectionPriorit : null ");
//        }
    }

}
