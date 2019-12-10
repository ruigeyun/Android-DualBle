/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.event;

import android.bluetooth.BluetoothDevice;

public class AddScanDeviceEvent {
    private BluetoothDevice bluetoothDevice;

    public AddScanDeviceEvent(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ScanEvent{");
        sb.append("bluetoothDevice=").append(bluetoothDevice);
        sb.append('}');
        return sb.toString();
    }
}
