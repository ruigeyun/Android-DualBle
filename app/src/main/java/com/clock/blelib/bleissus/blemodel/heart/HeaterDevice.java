/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel.heart;

import android.bluetooth.BluetoothDevice;

import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

import java.util.UUID;

public class HeaterDevice extends BLEAppDevice {
    private final String TAG = "BLELedDevice";
    public static final Integer DEVICE_TYPE_ID = 1001;
    public static final UUID SERVICE_UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
    private final UUID RX_CHAR_UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805f9b34fb");
    private final UUID TX_CHAR_UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805f9b34fb");

    public int powerState = 1;
    public String name = "";
    public String image = "";

    public int timer = 3;
    public int powerLevel = 1;

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

    public HeaterDevice() {
        isVirtualDevice = true;
    }

    public HeaterDevice(BluetoothDevice device, DataParserAdapter adapter) {

        super(device, adapter);

    }

}
