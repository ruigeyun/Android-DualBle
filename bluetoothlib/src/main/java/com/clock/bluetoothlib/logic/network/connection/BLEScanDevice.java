package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;

public class BLEScanDevice {
    BluetoothDevice scanBle;
    int deviceType;

    BLEScanDevice(BluetoothDevice scanBle, int deviceType) {
        this.scanBle = scanBle;
        this.deviceType = deviceType;
    }

}
