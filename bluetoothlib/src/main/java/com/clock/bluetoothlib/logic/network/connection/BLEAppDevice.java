/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;

import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

public abstract class BLEAppDevice extends BLELogicDevice {
    /**
     * 没有被扫描出来，但是之前连接过，需要显示在列表，这样的设备定义为虚拟设备，在界面手动连接虚拟设备，需启动扫描，扫描到再连接，非虚拟设备，不需要再启动扫描。
     * 虚拟设备连接成功后，将在列表中替换，所以这边变量没有清空处理
     */
    public boolean isVirtualDevice = false;

    public BLEAppDevice() {}

    public BLEAppDevice(BluetoothDevice device, DataParserAdapter adapter){
        super(adapter);
    }

//    @Override
//    public UUID getServiceUUID() {
//        return null;
//    }
//
//    @Override
//    public UUID getRxUUID() {
//        return null;
//    }
//
//    @Override
//    public UUID getTxUUID() {
//        return null;
//    }

}
