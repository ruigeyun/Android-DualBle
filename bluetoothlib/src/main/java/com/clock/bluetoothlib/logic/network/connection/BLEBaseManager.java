/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.clock.bluetoothlib.logic.network.DeviceAuthorizeListener;
import com.clock.bluetoothlib.logic.network.DeviceInfoSyncListener;
import com.clock.bluetoothlib.logic.network.data.BLEAllDispatcher;
import com.clock.bluetoothlib.logic.network.data.BLEPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public abstract class BLEBaseManager implements BLEServerListener {

    private final String TAG = "BLEBaseManager";
    // 只能初始化一次
    private boolean isBleInit = false;
    // 预防系统蓝牙还没启动完成，就执行扫描的情况
    private int autoConnCnt = 0;

    private BLEServerCentral mBLEServerCentral;
    private BLEScanner mBLEScanner;
    private BLEConnector mBLEConnector;
    private Timer scanTimer; // 扫描定时器
    /**
     * APP曾连接过的，APP启动后，就要自动去连的设备
     */
    private List<String> bleAutoToConn = new ArrayList();
    /**
     * APP自动扫描、连接指定的设备，包括1：APP启动后，自动连接之前连接过的设备；
     */
    private boolean isAutoScanConnMode = false;
    /**
     * 没有被扫描出来，但是之前连接过，需要显示在列表，这样的设备定义为虚拟设备，
     * 在界面手动连接虚拟设备，需启动扫描，扫描到再连接，非虚拟设备，不需要再启动扫描，
     * 连接虚拟设备，先扫描，如果在扫描过程中，其他设备要去连，得先等虚拟设备连接完
     */
    private boolean isConnVirtualDevice = false;

    private int autoScanConnDeviceCnt = 0;
    /**
     * 自动扫描、连接模式，连接设备的个数
     * 默认只连一个，其他直接显示
     */
    protected int autoScanConnMaxNum = 1;
    /**
     * 通过设备的serviceUUID，过滤扫描结果
     * 1、只有一种类型的设备serviceUUID，默认是过滤的，扫描结果只显示这种设备；
     * 2、多种设备，则会把周围的所有蓝牙设备扫描出来。蓝牙api，目前无法同时过滤多种serviceUUID，因为源码中过滤时是与的关系，不是或。
     * 遇到过一些蓝牙模块有问题，过滤则无法扫描出来，则需要设置此变量为false；
     */
    protected boolean isScanFilter = true;
    /**
     * 扫描停止时间，默认10s，单位ms
     */
    protected int scanTimeOut = 10000;

    public BLEBaseManager() {}

    /**
     * 自定义了多种设备，把这些设备的deviceTypeId、serviceUUID，添加对这个map；
     * 框架连接上设备后，根据serviceUUID，分辨是哪种类型的设备，然后通过onCreateDevice(BluetoothDevice bluetoothDevice, int deviceTypeId)，回调给用户，
     * 用户在接口里，根据deviceTypeId new一个设备返回给框架
     * @return 非 null
     */
    protected abstract HashMap<Integer, UUID> onGetDevicesServiceUUID();

    /**
     * 开启蓝牙使能后，第一步调用此方法
     * @param context
     */
    public synchronized void initBle(Context context) {
        if(!isBleInit) {
            isBleInit = true;
            mBLEScanner = new BLEScanner(context,onGetDevicesServiceUUID(),isScanFilter,this);

            mBLEServerCentral = BLEServerCentral.getInstance();
            mBLEServerCentral.init(context, this);

            mBLEConnector = BLEConnector.getInstance();
            mBLEConnector.init(context, onGetDevicesServiceUUID(), this);

            BLEAllDispatcher.getInstance().mBLEServerListener = this;
        }
    }

    public boolean isBleEnable() {
        return mBLEScanner.isBleEnable();
    }

    /**
     * 手动扫描蓝牙设备
     */
    public synchronized boolean scanBLEDeviceManual() {
        bleAutoToConn.clear(); // 手动去扫描，把开机自动扫描连接的列表清空，否则，这样也去连他们
        boolean ret = mBLEScanner.scanBLE();
        if (ret) {
            managerScanTime(scanTimeOut);
        }
        return ret;
    }

    public synchronized void stopScanDevice() {
        if(mBLEScanner.mBLEScanState != BLEState.Scan.Scanned) {
            mBLEScanner.stopBleScan();
            onScanOver();
        }
    }

    /**
     * 连接扫描到的某个蓝牙设备
     * @param bluetoothDevice
     */
    public void connScannedBleDevice(final BluetoothDevice bluetoothDevice) {
        abortScanTimer();
        stopScanDevice(); // 正常添加连接 连接前，断开扫描，这样快一点
        mBLEConnector.connToScannedBleDevice(bluetoothDevice);
    }

    /**
     * 自动扫描连接 曾经连接过的设备，只有在启动APP时执行的操作
     */
    public void autoScanConnConnectedDevice(List<String> connectedMac) {
        bleAutoToConn.clear();
        bleAutoToConn.addAll(connectedMac);
        autoScanConnDeviceCnt = 0;
        isAutoScanConnMode = true;
        autoScanDevice();
    }

    /**
     * 虚拟设备：APP曾连接过，但现在扫描不到的设备，没办法给它分配一个实体对象。手动去连它时，得启动扫描。
     * 自动扫描连接虚拟设备，每次只有一个虚拟设备去连接
     */
    public int autoScanConnVirtualDevice(List<String> connectedMac) {
        int connectingDevice = mBLEConnector.getConnectingDeviceNum();
        if (connectingDevice > 0) {
            Log.w(TAG, "1 想手动重连设备，但是有其他设备在连，只能退出等待： " + connectingDevice + " " + connectedMac);
            return -1;
        }
        int disConnDevice = mBLEServerCentral.getDisConnDeviceNum();
        if (disConnDevice >0) {
            Log.w(TAG, "1 想手动重连设备，但是有其他设备 还没释放完，只能退出等待: " + disConnDevice + " " + connectedMac);
            return -3;
        }
        if (isConnVirtualDevice) {
            Log.w(TAG, "想手动重连设备，但是正在连虚拟设备，请等待 " + " " + connectedMac);
            return -30;
        }
        bleAutoToConn.clear();
        bleAutoToConn.addAll(connectedMac);

        isConnVirtualDevice = true;

        autoScanDevice();
        return 0;
    }

    /**
     * 系统蓝牙还没启动完成，就执行扫描，多试几次，直到蓝牙启动完成
     */
    private void autoScanDevice() {
        if(!mBLEScanner.isBleEnable()) {
            Log.w(TAG, "scanBLEDevic: ble not enable");
            autoConnCnt++;
        }
        else {
            autoConnCnt = 0;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(autoConnCnt == 0) {
                    Log.w(TAG, "蓝牙初始化 s后，开始扫描设备" );
                    boolean ret = mBLEScanner.scanBLE();
                    if (ret) {
                        managerScanTime(scanTimeOut);
                    }
                }
                else {
                    if(autoConnCnt >5 ) {
                        autoConnCnt = 0;
                        Log.w(TAG, "蓝牙还没启动成功，bu zai 重来" );
                        return;
                    }
                    Log.w(TAG, "蓝牙还没启动成功，再重来" );
                    autoScanDevice();
                }
            }
        }, 500);
    }

    /**
     * 管理扫描时间, n 秒后终止扫描
     */
    private void managerScanTime(int timeOut) {
        if(scanTimer != null) {
            Log.w(TAG, "scanTimer cancel");
            scanTimer.cancel();
        }
        scanTimer = new Timer();
        scanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopScanDevice();
                bleAutoToConn.clear();
                isAutoScanConnMode = false;
                if (isConnVirtualDevice) {
                    isConnVirtualDevice = false;
                    onScanConnVirtualDevice(0);
                }
            }
        }, timeOut);
    }

    private void abortScanTimer() {
        if(scanTimer != null) {
            Log.w(TAG, "scanTimer abortScanTim");
            scanTimer.cancel();
            scanTimer = null;
        }
    }

    /**
     * 手动连接断开的设备
     * @param id
     * @return
     */
    public int manualConnDevice(final Integer id) {
        if (isConnVirtualDevice) {
            Log.w(TAG, "想手动重连设备，但是正在连虚拟设备，请等待 " + " " + id);
            return -30;
        }
        return mBLEServerCentral.manualConnBle(id);
    }

    /**
     * 删除蓝牙设备
     * @param id
     */
    public void removeDevice(Integer id) {
        mBLEServerCentral.removeBle(id);
    }

    /**
     * 断开蓝牙设备的连接
     * @param id
     */
    public int forceDisConnDevice(final Integer id) {
        if (isConnVirtualDevice) {
            Log.w(TAG, "想手动重连设备，但是正在连虚拟设备，请等待 " + " " + id);
            return -30;
        }
        return mBLEServerCentral.forceDisConnBle(id);
    }

    public boolean hasDeviceConnecting() {
        int cnt = mBLEConnector.getConnectingDeviceNum();
        if (cnt == 0) {
            return false;
        }
        return true;
    }

    /**
     * 发送蓝牙透传数据
     * @param data 数据
     * @param bleId 蓝牙设备mDeviceId
     */
    public void sendBLETransmitData(final byte[] data, final int bleId) {
        mBLEServerCentral.sendTransmitData(data, bleId);
    }

//    public void sendBLEConfigData(final byte[] data, final int bleId) {
//        mBLEServerCentral.sendConfigData(data, bleId);
//    }

    @Override
    public final void onScannedDevice(BluetoothDevice bluetoothDevice) {
        if (bleAutoToConn.contains(bluetoothDevice.getAddress())) {
            Log.d(TAG, "自动连接ble 包含其中");
            if (isConnVirtualDevice) {
                abortScanTimer();
                stopScanDevice(); // 每次只连一个虚拟设备，扫描到了，就停止扫描了，下一步去连了
                mBLEConnector.connToVirtualBleDevice(bluetoothDevice);
                isConnVirtualDevice = false;
            }
            else if(isAutoScanConnMode) {
                autoScanConnDeviceCnt++;
                if (autoScanConnDeviceCnt >= autoScanConnMaxNum) {
                    Log.w(TAG, "开机自动扫描连接模式 ，已经连了 "  + autoScanConnMaxNum + " 个设备，断开扫描，停止其他连接");
                    abortScanTimer();
                    stopScanDevice();
                    isAutoScanConnMode = false; // 扫描结束，模式去掉
                }

                if (autoScanConnDeviceCnt <= autoScanConnMaxNum) {
                    mBLEConnector.connToAutoScanBleDevice(bluetoothDevice);
                }
            }
        }
    }

    @Override
    public abstract void onScanOver();

    @Override
    public abstract void onAddScanDevice(BluetoothDevice bluetoothDevice);

    @Override
    public void onScanConnVirtualDevice(int type) {

    }
    @Override
    public abstract void onConnectUnTypeDevice(BluetoothDevice bluetoothDevice, int type);

    @Override
    public abstract BLEAppDevice onCreateDevice(BluetoothDevice bluetoothDevice, int deviceType);

    @Override
    public abstract void onConnectDevice(BLEAppDevice device, int type);
    @Override
    public void onCharacterMatch(BluetoothDevice device, int type) {

    }
    @Override
    public void onAuthorizeDevice(BLEAppDevice device, DeviceAuthorizeListener listener) {
        listener.onAuthorize(true);
    }
    @Override
    public void onDoNecessaryBiz(BLEAppDevice device, DeviceInfoSyncListener listener) {
        listener.onInfoSync(1);
    }
    @Override
    public void onAddPreDevice(BluetoothDevice device){}

    @Override
    public abstract void onAddNewDevice(BLEAppDevice device);

    @Override
    public void onReconnectDevice(BLEAppDevice device) {

    }
    @Override
    public abstract void onUpdateDeviceInfo(BLEAppDevice device);

    @Override
    public abstract void onDeviceSendResult(String result);

    @Override
    public abstract void onDevicesRespOriginalData(BLEPacket message);
    @Override
    public abstract void onDeviceRespSpliceData(BLEPacket message);
}
