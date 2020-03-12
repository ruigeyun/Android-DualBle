/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

final class BLEScanner implements BluetoothAdapter.LeScanCallback {

    private final String TAG = "BLEScanner";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private UUID SERVICE_UUID;

    BLEState.Scan mBLEScanState = BLEState.Scan.ScanIdle;
//    long scanStartTime = 0;
    ArrayList<BLEScanDevice> deviceScanList = new ArrayList<>();// 扫描到的蓝牙设备
    private HashMap<Integer, UUID> deviceServiceUUIDMap;

    private BLEServerListener mListener;

    private static BLEScanner instance = new BLEScanner();
    public static BLEScanner getInstance() {
        return instance;
    }
    private BLEScanner() {}

    void init(Context context, HashMap<Integer, UUID> serverUuidMap, boolean isScanFilter, BLEServerListener listener) {

        this.deviceServiceUUIDMap = serverUuidMap;
        Log.d(TAG, "init scanner " + serverUuidMap + " " + serverUuidMap.keySet().size() + " " +isScanFilter);
        if (serverUuidMap!=null && serverUuidMap.keySet().size()==1 && isScanFilter) {
            LogUtil.d(TAG, "serviceUuid: " + serverUuidMap.values().iterator().next());
            init(context, serverUuidMap.values().iterator().next(), listener);
        }
        else {
            init(context, null, listener);
        }

    }

    private void init(Context context, UUID uuid, BLEServerListener listener) {
        SERVICE_UUID = uuid;
        mListener = listener;

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        else {
            Log.w(TAG, "init: bluetoothManager null");
        }

        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }

        Log.w(TAG, "init: BLEServerCentral");
    }

    boolean isBleEnable() {
        return bluetoothAdapter.isEnabled();
    }

    boolean scanBLE() {

        deviceScanList.clear();
        if(mBLEScanState == BLEState.Scan.Scanning) {
            Log.d(TAG, "scan: is scanning just waiting");
            return false;
        }
//        scanStartTime = System.currentTimeMillis();
        Log.d(TAG, "scan: start scan my ");
        mBLEScanState = BLEState.Scan.Scanning;

        UUID[] serviceUuids = new UUID[]{SERVICE_UUID};
//        UUID[] serviceUuids = new UUID[]{BLELedDevice.SERVICE_UUID, UUID_Service_MH}; // 不能这样，这是全部包含，才扫描到，是与的关系，不是或
        if (SERVICE_UUID == null) {
            return bluetoothAdapter.startLeScan(this);
        }
        else {
            return bluetoothAdapter.startLeScan(serviceUuids, this);
        }
    }

    void stopBleScan() {
        Log.d(TAG, "stopBleSca... ");
        if(mBLEScanState != BLEState.Scan.Scanned) {
            bluetoothAdapter.stopLeScan(this);
            mBLEScanState = BLEState.Scan.Scanned;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecordBytes) {

//        if (deviceScanList.contains(bluetoothDevice)) {
////            Log.d(TAG, "onLeScan: 已经包含这设备了");
//            return;
//        }
        for (BLEScanDevice device: deviceScanList) {
            if (device.scanBle.equals(bluetoothDevice)) {
                Log.d(TAG, "onLeScan: 已经包含这设备了");
                return;
            }
        }
        Log.v(TAG, "onLeScan device: " + bluetoothDevice.getAddress() + " "
                + bluetoothDevice.getName() + " " + bluetoothDevice.getBondState() + " 信号强度：" + rssi
                + " " + bluetoothDevice.getUuids()); // uuid 为本地缓存的UUID，通常为null

//        Log.i(TAG, "广播包：" + "len: " +scanRecordBytes.length + " 数据：" + BleLibByteUtil.BytesToHexStringPrintf(scanRecordBytes));
        // 02 01 06 03 03 f0 ff 0d 09 53 74 65 6c 6c 61 20 53 6d 61 72 74 0d 09 73 74 65 6c 6c 61 20 53 6d 61 72 74 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        // 02 01 06 03 03 a0 fe 10 08 4e 33 30 32 2d 4c 43 44 2d 30 31 31 34 30 34 07 ff c6 db 0e 6f 7c b8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        ParsedAd ad = parseData(scanRecordBytes);
        Log.i(TAG, "广播包解析得：" + ad);
        if (ad.uuids == null || ad.uuids.size()<=0) {
            Log.d(TAG, "扫描到的设备广播无server uuid，忽略它" );
            // 处理广播无uuid的设备，一种思路，定义设备类型为-1的无广播设备，在这个地方创建设备，连接（常规）。但也只能创建有一种设备，一个service uuid
            return;
        }
        UUID deviceUuid = ad.uuids.get(0);
        if (deviceServiceUUIDMap.containsValue(deviceUuid)) {
            int deviceType = -1; // 根据serviceUUID，确定是哪一种设备
            for(Integer key : deviceServiceUUIDMap.keySet()){
                if (deviceServiceUUIDMap.get(key).equals(deviceUuid)) {
                    deviceType = key;
                    break;
                }
            }
            LogUtil.d(TAG, "扫描的 deviceType : " + deviceType);
            Log.w(TAG, "onLeScan: 把设备加入到扫描列表 " + bluetoothDevice.toString());
            deviceScanList.add(new BLEScanDevice(bluetoothDevice, deviceType));

            mListener.onScannedDevice(bluetoothDevice);
            mListener.onAddScanDevice(bluetoothDevice);
        }
        else {
            Log.d(TAG, "扫描到的设备的server uuid 不在范围内" );
            return;
        }

    }

    public ParsedAd parseData(byte[] adv_data) {
        ParsedAd parsedAd = new ParsedAd();
        ByteBuffer buffer = ByteBuffer.wrap(adv_data).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0)
                break;

            byte type = buffer.get();
            length -= 1;
            switch (type) {
                case 0x01: // Flags
                    parsedAd.flags = buffer.get();
                    length--;
                    break;
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                case 0x14: // List of 16-bit Service Solicitation UUIDs
                    while (length >= 2) {
                        parsedAd.uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;
                case 0x04: // Partial list of 32 bit service UUIDs
                case 0x05: // Complete list of 32 bit service UUIDs
                    while (length >= 4) {
                        parsedAd.uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getInt())));
                        length -= 4;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                case 0x15: // List of 128-bit Service Solicitation UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        parsedAd.uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
                case 0x08: // Short local device name
                case 0x09: // Complete local device name
                    byte sb[] = new byte[length];
                    buffer.get(sb, 0, length);
                    length = 0;
                    parsedAd.localName = new String(sb).trim();
                    break;
                case (byte) 0xFF: // Manufacturer Specific Data
                    parsedAd.manufacturer = buffer.getShort();
                    length -= 2;
                    break;
                default: // skip
                    break;
            }
            if (length > 0) {
                buffer.position(buffer.position() + length);
            }
        }
        return parsedAd;
    }

    class ParsedAd {
        public byte flags;
        public List<UUID> uuids = new ArrayList<>();
        public String localName;
        public short manufacturer;

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ParsedAd{");
            sb.append("flags=").append(flags);
            sb.append(", uuids=").append(uuids);
            sb.append(", localName='").append(localName).append('\'');
            sb.append(", manufacturer=").append(manufacturer);
            sb.append('}');
            return sb.toString();
        }
    }

}

