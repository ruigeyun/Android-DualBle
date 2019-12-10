/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.event;


import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;

/**
 * 连接扫描列表的设备，有些设备无法识别，则删除
 */
public class ConnectDeviceEvent {

    public BLEAppDevice device;
    public int type; // 连接反馈，无法识别的设备，删除

    public ConnectDeviceEvent(BLEAppDevice device, int type) {
        this.device = device;
        this.type = type;
    }
}
