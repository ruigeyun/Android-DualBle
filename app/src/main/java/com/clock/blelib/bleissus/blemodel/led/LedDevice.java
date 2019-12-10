/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel.led;

import android.bluetooth.BluetoothDevice;

import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

import java.util.UUID;

public class LedDevice extends BLEAppDevice {
    private final String TAG = "BLELedDevice";

    public static final Integer DEVICE_TYPE_ID = 1002;
    public static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final UUID RX_CHAR_UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    private final UUID TX_CHAR_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");

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
