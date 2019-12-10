/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.event;


import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;

public class AddPreConnDeviceEvent {
    public BLEAppDevice device;

    public AddPreConnDeviceEvent(BLEAppDevice device) {
        this.device = device;
    }
}
