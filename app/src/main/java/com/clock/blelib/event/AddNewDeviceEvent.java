/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.event;


import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;

/**
 * 连接上新设备，到设备列表中显示
 */
public class AddNewDeviceEvent {

    public BLEAppDevice device;

    public AddNewDeviceEvent(BLEAppDevice device) {
        this.device = device;
    }

}