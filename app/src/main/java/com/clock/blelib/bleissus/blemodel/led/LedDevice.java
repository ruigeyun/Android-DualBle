/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel.led;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.clock.bluetoothlib.logic.network.DeviceAuthorizeListener;
import com.clock.bluetoothlib.logic.network.DeviceInfoSyncListener;
import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

import java.util.UUID;

public class LedDevice extends BLEAppDevice {
    private final String TAG = "BLELedDevice";

    /**
     * 此DEVICE_TYPE_ID 不能和别的设备定义的值一样
     */
    public static final Integer DEVICE_TYPE_ID = 1002;
    public static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final UUID RX_CHAR_UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    private final UUID TX_CHAR_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");
    /**
     *  部分缺陷设备，广播的server uuid 和 实际的server uuid不一致，就多定义一个广播uuid，扫描过滤时，用它，连接成功后，用实际的uuid
     */
    public static final UUID AD_UUID = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
    @Override
    public UUID getServiceUUID() {
        return SERVICE_UUID;
    }
    @Override
    public UUID getRxUUID() {
        return RX_CHAR_UUID;
    }
    @Override
    public UUID getTxUUID() {
        return TX_CHAR_UUID;
    }

    public String dualColor = "";
    public String hardwareVersion = "";
    public int powerState = 0;
    public String nickname = "";

    public LedDevice(BluetoothDevice device, DataParserAdapter adapter) {
        super(device, adapter);

    }

    @Override
    public void onAuthorizeDevice(BLEAppDevice device, DeviceAuthorizeListener listener) {
        sendPwdToBleDevice(listener);
    }

    @Override
    public void onDoNecessaryBiz(BLEAppDevice device, DeviceInfoSyncListener listener) {
        new BLELedNecessaryBiz(device, listener).getDeviceInfo();

    }

    private void sendPwdToBleDevice(DeviceAuthorizeListener listener) {
        Log.d(TAG, "密码验证: "  + mDeviceId);
//        Cmd71Pwd cmd = new Cmd71Pwd();
//        DataSendTransferBle.writeBleDevice(cmd, mOwnerDevice.mDeviceId, new OnRespResultListener() {
//            @Override
//            public void onSuccess(int code) {
//                Log.i("test", "--led 蓝牙-密码验证成功--=");
//                listener.onAuthorize(true); // 验证成功后，回调true
//            }
//
//            @Override
//            public void onFailure(int code) {
//                Log.i("test", "--led 蓝牙-密码验证失败--=");
//            }
//        });
        // 验证成功后
        listener.onAuthorize(true); // 验证成功后，回调true
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LedDevice{");
        sb.append("dualColor='").append(dualColor).append('\'');
        sb.append(", hardwareVersion='").append(hardwareVersion).append('\'');
        sb.append(", powerState=").append(powerState);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
