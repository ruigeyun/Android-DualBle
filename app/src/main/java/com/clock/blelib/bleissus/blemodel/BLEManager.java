/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.clock.blelib.bleissus.blemodel.heart.HeaterDevice;
import com.clock.blelib.bleissus.blemodel.led.LedDataAdapter;
import com.clock.blelib.bleissus.blemodel.led.LedDevice;
import com.clock.blelib.event.AddNewDeviceEvent;
import com.clock.blelib.event.AddScanDeviceEvent;
import com.clock.blelib.event.BleSendResultEvent;
import com.clock.blelib.event.ConnectDeviceEvent;
import com.clock.blelib.event.ConnectUnTypeDeviceEvent;
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
        map.put(HeaterDevice.DEVICE_TYPE_ID, HeaterDevice.SERVICE_UUID);
        map.put(LedDevice.DEVICE_TYPE_ID, LedDevice.SERVICE_UUID);

        return map;
    }

    @Override
    public void onScanOver() {
        Log.w(TAG, "onScanOve。。");
    }

    @Override
    public BLEAppDevice onCreateDevice(BluetoothDevice bluetoothDevice, int deviceType) {
        if (deviceType == HeaterDevice.DEVICE_TYPE_ID) {
            //数据包解析适配器为null，蓝牙设备回应的数据在 onDevicesRespOriginalData(BLEPacket message)
            return new HeaterDevice(bluetoothDevice, null);
        }
        else if (deviceType == LedDevice.DEVICE_TYPE_ID) {
            // 设置了数据包解析适配器，数据回调在 onDeviceRespSpliceData(BLEPacket message)
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
    public void onConnectUnTypeDevice(BluetoothDevice bluetoothDevice, int type) {
        EventBus.getDefault().post(new ConnectUnTypeDeviceEvent(bluetoothDevice, type));
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
        LogUtil.i(TAG, "onDeviceRespSpliceDat: [" + BytesUtil.BytesToHexStringPrintf(message.bleData) + "] bleId: " + message.bleId);
//        DataManager.getInstance().DecodeRespData(message.bleData, message.bleId);
    }

    @Override
    public void onDevicesRespOriginalData(BLEPacket message) {
        LogUtil.v(TAG, "onDevicesRespOriginalDat: [" + BytesUtil.BytesToHexStringPrintf(message.bleData) + "] bleId: " + message.bleId);
    }


}
