/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.clock.blelib.bleissus.blemodel.bracelet.BraceletDevice;
import com.clock.blelib.bleissus.blemodel.heart.HeaterDataAdapter;
import com.clock.blelib.bleissus.blemodel.heart.HeaterDevice;
import com.clock.blelib.bleissus.blemodel.led.LedDataAdapter;
import com.clock.blelib.bleissus.blemodel.led.LedDevice;
import com.clock.blelib.event.AddNewDeviceEvent;
import com.clock.blelib.event.AddScanDeviceEvent;
import com.clock.blelib.event.BleSendResultEvent;
import com.clock.blelib.event.ConnectDeviceEvent;
import com.clock.blelib.event.updateDeviceInfoEvent;
import com.clock.blelib.util.BytesUtil;
import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.connection.BLEBaseManager;
import com.clock.bluetoothlib.logic.network.data.BLEPacket;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.UUID;

public class BLEManager extends BLEBaseManager {

    private final String TAG = "BLEManager";

    private static BLEManager instance = new BLEManager();
    public static BLEManager getInstance() {
        return instance;
    }

    @Override
    public HashMap<Integer, UUID> onGetDevicesServiceUUID() {
        HashMap<Integer, UUID> map = new HashMap();
        map.put(BraceletDevice.DEVICE_TYPE_ID, BraceletDevice.SERVICE_UUID);
        map.put(HeaterDevice.DEVICE_TYPE_ID, HeaterDevice.SERVICE_UUID);
        map.put(LedDevice.DEVICE_TYPE_ID, LedDevice.AD_UUID);

        return map;
    }

    @Override
    public void onScanOver() {
        Log.w(TAG, "onScanOve。。");
    }

    @Override
    public BLEAppDevice onCreateDevice(BluetoothDevice bluetoothDevice, int deviceType) {
        if (deviceType == BraceletDevice.DEVICE_TYPE_ID) {
            //数据包解析适配器为null，蓝牙设备回应的数据在 onDevicesRespOriginalData(BLEPacket message)
            return new BraceletDevice(bluetoothDevice, null);
        }
        else if (deviceType == HeaterDevice.DEVICE_TYPE_ID) {
            // 设置了数据包解析适配器，数据回调在 onDeviceRespSpliceData(BLEPacket message)
            return new HeaterDevice(bluetoothDevice, new HeaterDataAdapter());
        }
        else if (deviceType == LedDevice.DEVICE_TYPE_ID) {
            return new LedDevice(bluetoothDevice, new LedDataAdapter());
        }
        else {
            return null;
        }
    }

    @Override
    public void onAddScanDevice(BluetoothDevice bluetoothDevice){
        EventBus.getDefault().post(new AddScanDeviceEvent(bluetoothDevice));
    }

    @Override
    public void onConnectDevice(BLEAppDevice device, int type){
        EventBus.getDefault().post(new ConnectDeviceEvent(device, type));
    }

    @Override
    public void onAddNewDevice(BLEAppDevice device){
        EventBus.getDefault().post(new AddNewDeviceEvent(device));
    }
    @Override
    public void onUpdateDeviceInfo(BLEAppDevice device) {
        EventBus.getDefault().post(new updateDeviceInfoEvent(device));
    }
    @Override
    public void onDeviceSendResult(String result){
        EventBus.getDefault().post(new BleSendResultEvent(result));
    }

    @Override
    public void onDeviceRespSpliceData(BLEPacket message) {
        //定义了数据包解析适配器的设备，通过这个方法回调数据， bleDeviceId标志不同的蓝牙设备，也就是设备的mDeviceId
        LogUtil.i(TAG, "onDeviceRespSpliceDat: [" + BytesUtil.BytesToHexStringPrintf(message.bleData) + "] bleDeviceId: " + message.bleDeviceId);
//        DataManager.getInstance().DecodeRespData(message.bleData, message.bleDeviceId);

    }

    @Override
    public void onDevicesRespOriginalData(BLEPacket message) {
        //数据包解析适配器为null的设备，通过这个方法回调数据， bleDeviceId标志不同的蓝牙设备，也就是设备的mDeviceId
        LogUtil.v(TAG, "onDevicesRespOriginalDat: [" + BytesUtil.BytesToHexStringPrintf(message.bleData) + "] bleDeviceId: " + message.bleDeviceId);

    }


}
