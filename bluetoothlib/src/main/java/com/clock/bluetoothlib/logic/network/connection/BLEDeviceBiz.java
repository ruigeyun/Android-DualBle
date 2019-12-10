/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;


import com.clock.bluetoothlib.logic.network.BleLibByteUtil;
import com.clock.bluetoothlib.logic.network.DeviceAuthorizeListener;
import com.clock.bluetoothlib.logic.network.DeviceInfoSyncListener;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

final class BLEDeviceBiz {
    private final String TAG = "BLEOwnerBiz";
    private BLELogicDevice mOwnerDevice;

    private Timer reConnectTimer;

    private BLEState.Reconnect reConnectState = BLEState.Reconnect.ReConnIdle;

    private int ReconnectIntervalTime = 3000; // 断开 间隔 3秒后，才重连
    private int ReconnectMaxTime = 10000 + ReconnectIntervalTime;
//    private int reconnectCnt = 0;

    BLEDeviceBiz(BLELogicDevice bleDevice) {
        mOwnerDevice = bleDevice;
    }

    void clearResource() {
        mOwnerDevice = null;
        if(reConnectTimer != null) {
            reConnectTimer.cancel();
        }
    }

    void clearTimer() {
        if(reConnectTimer != null) {
            reConnectTimer.cancel();
        }
    }

    void doConnectedAction() {
        LogUtil.w(TAG, "doConnectedActio", (BLEAppDevice) mOwnerDevice);
        reConnectState = BLEState.Reconnect.ReConnIdle;
        if(reConnectTimer != null) {
            LogUtil.w(TAG, "Connected reConnectTimer cancel", (BLEAppDevice) mOwnerDevice);
            reConnectTimer.cancel();
            reConnectTimer = null;
        }
    }

    /**
     * 设备断线，重连，10秒还没连接成功，不再连。由 BLELogicDevice 类控制
     * 重连过程，如果(30秒)一直没有连上，会回调BluetoothProfile.STATE_DISCONNECTED
     */
    void doDisconnectAction() {
        LogUtil.w(TAG, "doDisconnectActio..", (BLEAppDevice) mOwnerDevice);
        mOwnerDevice.mBLEConnState = BLEState.Connect.Disconnect;
        mOwnerDevice.updateDeviceInfo(null);

        mOwnerDevice.clearBleConnResource();

//        if(reConnectState == BLEState.Reconnect.Reconnecting) {
//            LogUtil.w(TAG, "doDisconnectActio: is reconnceting just be Hold ", (BLEAppDevice) mOwnerDevice);
//            return;
//        }
        reConnectState = BLEState.Reconnect.Reconnecting;
        if(reConnectTimer != null) {
            LogUtil.w(TAG, "reConnectTimer cancel", (BLEAppDevice) mOwnerDevice);
            reConnectTimer.cancel();
        }
        reConnectTimer = new Timer();
        // 如果采取5秒重连一次，最终连接成功时，会多次回调callback接口，不可取
        reConnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                autoReconnectBle();
            }
        }, ReconnectIntervalTime);
    }

    private void autoReconnectBle() {
        LogUtil.w(TAG, "auto re conn", (BLEAppDevice) mOwnerDevice);
        if (mOwnerDevice.mBLEConnState == BLEState.Connect.ForceDisconnect) {
            if(reConnectTimer != null) {
                reConnectTimer.cancel();
            }
            mOwnerDevice.connectFailAction();
            LogUtil.w(TAG, "设备已经被强行断开，不再去自动重连", (BLEAppDevice) mOwnerDevice);
            return;
        }

        mOwnerDevice.connBleSelf();
    }

    /**
     * 特征都配置好后，进入下一阶段，密码校验
     */
    void doCharacterEnableAction() {
        mOwnerDevice.mBLEConnState = BLEState.Connect.Enable;
        mOwnerDevice.mBLEServerListener.onAuthorizeDevice((BLEAppDevice) mOwnerDevice, new DeviceAuthorizeListener() {
            @Override
            public void onAuthorize(boolean isAuthorize) {
                if (isAuthorize) {
                    mOwnerDevice.mBLEConnState = BLEState.Connect.Authorize;
                    doAuthorizeSuccessAction();
                }
                else {
                    mOwnerDevice.mDeviceStatusListener.onRemove(mOwnerDevice);
                }
            }
        });
    }
    /**
     * APP与蓝牙设备密码校验通过后，进入下一阶段，同步信息
     */
    private void doAuthorizeSuccessAction() {
        // 第一次连接上的设备，才添加到主页设备列表，重连的设备，只需要更新状态
        if (mOwnerDevice.isDeviceFirstAdd) {
            mOwnerDevice.mBLEServerListener.onAddNewDevice((BLEAppDevice) mOwnerDevice);
            mOwnerDevice.isDeviceFirstAdd = false;
        }
        else {
            mOwnerDevice.mBLEServerListener.onReconnectDevice((BLEAppDevice) mOwnerDevice);
        }

        // 完成APP与设备的信息同步，设备才处于正常业务交流，可用状态
        mOwnerDevice.mBLEServerListener.onDoNecessaryBiz((BLEAppDevice) mOwnerDevice, new DeviceInfoSyncListener() {
            @Override
            public void onInfoSync(int type) {
                if (mOwnerDevice == null) {
                    LogUtil.e(TAG, "设备已经被删除。。。");
                    return;
                }
                LogUtil.w(TAG, "all biz is ok ,set device activity", (BLEAppDevice) mOwnerDevice);
                mOwnerDevice.mBLEConnState = BLEState.Connect.Active;
                mOwnerDevice.mReconnectType = BLEState.ReconnectType.Auto;
                mOwnerDevice.updateDeviceInfo(null);
            }
        });
    }

    private void doConfigRespData(byte[] data) {
        LogUtil.i(TAG, "doConfigRespData: " + BleLibByteUtil.BytesToHexStringPrintf(data));

    }
}

