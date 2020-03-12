/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.clock.bluetoothlib.logic.network.DeviceAuthorizeListener;
import com.clock.bluetoothlib.logic.network.DeviceInfoSyncListener;

import java.util.UUID;

abstract class BLEBaseDevice {

    private final String TAG = "BLEBaseDevice";
    private final UUID UUID_CHARACTERISTIC_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    BluetoothGatt mBluetoothGatt;
    public int mDeviceId; // 每个ble 分配一个数字id，便于管理（回应数据 回调的handler发送消息，只能是int，用mac地址，6位，则超过了int，无奈之下，只能自己定义一个id）
    public BLEState.Connect mBLEConnState = BLEState.Connect.ConnectIdle; // ble的连接状态，只有Active状态才能操作它
    public DeviceState mDeviceState = DeviceState.Normal;
    public BLEState.ReconnectType mReconnectType = BLEState.ReconnectType.Manual; // 0 自动重连，1 手动重连（强制掉线后手动连它）
    public int mDeviceTypeId = 0; // 不同的蓝牙模块类型
    public String mBleAddress = ""; // 该ble的mac地址
    public String mBleName = "";

    /**
     * 连接ble本身
     */
    abstract void connBleSelf();

    /**
     * 手动连接断开的设备
     */
    abstract int manualConnBle();

    /**
     * 强制断开APP与蓝牙设备的连接
     */
    abstract void ForceDisConn();
    abstract void removeSelf();

    /**
     * 清空蓝牙连接相关的资源，为自动重连或下一次连接做清理工作
     */
    abstract void clearBleConnResource();

    /**
     * 清空对象占据的内存资源，释放对象
     */
    abstract void clearMemoryResources();
    abstract void writeConfigData(byte[] data);
    abstract void writeTransmitData(byte[] data);
    abstract boolean enableNotifyCharacter();
    /**
     * APP发送密码给设备验证，验证通过才能使用此设备，如果没有这个需求，就不必重新这个方法，框架默认校验通过
     * @param device
     */
    public void onAuthorizeDevice(BLEAppDevice device, DeviceAuthorizeListener listener) {
        listener.onAuthorize(true);
    }
    /**
     * 密码校验通过后，可以正式通讯，执行通讯前，都需要做的业务，比如获取蓝牙设备的状态信息，配置设备参数等.
     * 完成这个方法，蓝牙设备才进入 active 状态，正式被使用
     * 如果没有这个需求，就不必重新这个方法，框架默认通过
     * @param device
     */
    public void onDoNecessaryBiz(BLEAppDevice device, DeviceInfoSyncListener listener) {
        listener.onInfoSync(1);
    }

    public void updateDeviceInfo(Object info) { }

    boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        boolean ret = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (ret) {
            Log.w(TAG, " set Notify success");
        }
        else {
            Log.w(TAG, " set Notify fail");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_CONFIG_DESCRIPTOR);
        if(descriptor == null) {
            Log.w(TAG, "0 descriptor null ");
            return ret;
        }
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE:BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        boolean ret2 = mBluetoothGatt.writeDescriptor(descriptor);
        if (ret2) {
            Log.w(TAG, "descriptor Notify success");
        }
        else {
            Log.w(TAG, "descriptor Notify fail");
        }
        return ret2;
    }

    /**
     * indicate setting
     */
    boolean setCharacteristicIndication(BluetoothGattCharacteristic characteristic, boolean enable) {
        boolean ret = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (ret) {
            Log.w(TAG, "1 set Notify success");
        }
        else {
            Log.w(TAG, "1 set Notify fail");
            return ret;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_CONFIG_DESCRIPTOR);
        if(descriptor == null) {
            Log.w(TAG, "descriptor null ");
            return ret;
        }
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE:BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        boolean ret2 = mBluetoothGatt.writeDescriptor(descriptor);
        if (ret2) {
            Log.w(TAG, "descriptor INDICATION success");
        }
        else {
            Log.w(TAG, "descriptor INDICATION fail");
        }
        return ret2;
    }

}
