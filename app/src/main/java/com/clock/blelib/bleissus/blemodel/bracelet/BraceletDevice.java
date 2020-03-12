package com.clock.blelib.bleissus.blemodel.bracelet;

import android.bluetooth.BluetoothDevice;

import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

import java.util.UUID;

public class BraceletDevice extends BLEAppDevice {
    // 蓝牙手环
    private final String TAG = "BraceletDevice";
    /**
     * 此DEVICE_TYPE_ID 不能和别的设备定义的值一样
     */
    public static final Integer DEVICE_TYPE_ID = 1000;
    public static final UUID SERVICE_UUID = UUID.fromString("0000fea0-0000-1000-8000-00805f9b34fb");
    private final UUID RX_CHAR_UUID = UUID.fromString("0000fea1-0000-1000-8000-00805f9b34fb");
    private final UUID TX_CHAR_UUID = UUID.fromString("0000fea2-0000-1000-8000-00805f9b34fb");

    public int powerState = 1;
    public String nickname = "";

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

    public BraceletDevice(BluetoothDevice device, DataParserAdapter adapter) {

        super(device, adapter);

    }

}

