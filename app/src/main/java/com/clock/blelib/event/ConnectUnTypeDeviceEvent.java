/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.event;

import android.bluetooth.BluetoothDevice;

public class ConnectUnTypeDeviceEvent {
    public BluetoothDevice device;
    public int type; // 连接反馈，无法识别的设备，删除

    public ConnectUnTypeDeviceEvent(BluetoothDevice device, int type) {
        this.device = device;
        this.type = type;
    }
}
