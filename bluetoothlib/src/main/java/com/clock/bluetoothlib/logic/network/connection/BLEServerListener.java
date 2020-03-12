/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothDevice;

import com.clock.bluetoothlib.logic.network.data.BLEPacket;

public interface BLEServerListener {

    /**
     * 蓝牙扫描器扫到的结果，通知管理器，是否需要自动连接这些设备，用于自动扫描模式，库内使用，不对外开放，使用final禁止重写
     */
    public void onScannedDevice(BluetoothDevice bluetoothDevice);

    /**
     * 扫描结束
     */
    public void onScanOver();
    /**
     * 返回扫描到的ble
     */
    public void onAddScanDevice(BluetoothDevice bluetoothDevice);

    /**
     * 连接还没确定类型的设备的回调，目前只有连失败才回调，成功：1，失败：-
     */
    public void onScanConnVirtualDevice(int type);

    /**
     * 每成功连接一个ble，就创建一个ble的类实例，自己定义的蓝牙设备，在这个方法里面创建的
     * @param bluetoothDevice
     * @param deviceTypeId  自定义的类型id
     * @return 创建的ble实例，很重要
     */
    public BLEAppDevice onCreateDevice(BluetoothDevice bluetoothDevice, int deviceTypeId);

    /**
     * 扫描到设备后，连接设备，连接成功或失败，目前只有连接失败才调用
     * 包括自动重连
     * @param device
     * @param type 成功：1，失败：0
     */
    public void onConnectDevice(BLEAppDevice device, int type);

    /**
     * 连接成功后，发觉服务后，搜索符合自己的特性，成功：1，失败：0
     * @param device
     * @param type 成功：1，失败：0
     */
    public void onCharacterMatch(BluetoothDevice device, int type);
    /**
     * 准备去连接的设备，先显示出来，再连接。
     * 如果连接成功后再显示，就感觉比较慢
     * @param device
     */
    public void onAddPreDevice(BluetoothDevice device);
    /**
     * ble连接成功，特性配置成功，数据接收方式配置成功，后，返回这个ble实例
     * 只回调一次
     */
    public void onAddNewDevice(BLEAppDevice device);

    /**
     * 设备重连接成功后回调
     * 重连成功一次，回调一次
     * @param device
     */
    public void onReconnectDevice(BLEAppDevice device);

    /**
     * 更新设备信息（进入可操作状态、连接断开）
     * @param device
     */
    public void onUpdateDeviceInfo(BLEAppDevice device);

    /**
     * 发送ble数据，发送结果反馈，目前只在发送失败情况调用
     * @param result 内容
     */
    public void onDeviceSendResult(String result);

    /**
     * 蓝牙设备回应APP的原始数据
     * 分两种：
     * 1：APP发送，ble回应数据 2：ble主动上报数据
     * @param message
     */
    public void onDevicesRespOriginalData(BLEPacket message);

    /**
     * 蓝牙设备回应APP的数据，经过框架拼包算法处理，解决数据丢包、断包等问题，建议用此算法
     * @param message
     */
    public void onDeviceRespSpliceData(BLEPacket message);
}
