/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.event;


import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;

/**
 * 重连设备，分：自动连接，手动连接
 * 手动连接：指在设备列表的设备，被新设备顶下去了，再手动连接
 * 自动连接：设备蓝牙自爆异常，自动去连接，恢复状态
 */
public class ReconnectDeviceEvent {

    public BLEAppDevice device;

    public ReconnectDeviceEvent(BLEAppDevice device) {
        this.device = device;
    }
}
