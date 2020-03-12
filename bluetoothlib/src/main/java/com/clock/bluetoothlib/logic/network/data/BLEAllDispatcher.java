/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.data;

import com.clock.bluetoothlib.logic.network.connection.BLEServerListener;
import com.clock.bluetoothlib.logic.network.BleLibByteUtil;

import java.util.LinkedList;
import java.util.List;

public final class BLEAllDispatcher extends Thread {

    private final LinkedList<BLEPacket> packageQueue = new LinkedList<>();
    private boolean isStart = false;
    public BLEServerListener mBLEServerListener;

    private static BLEAllDispatcher instance = new BLEAllDispatcher();
    public static BLEAllDispatcher getInstance() {
        return instance;
    }
    private BLEAllDispatcher() {}

    void addAllPackageData(List<byte[]> data, int deviceId) {
        synchronized (packageQueue) {
            for (byte[] item: data) {
//                d("addAllPackageData : " + BytesUtil.BytesToHexStringPrintf(item));
                BLEPacket packet = new BLEPacket(item, deviceId);
                packageQueue.add(packet);
            }
        }
    }

    private void removePackageData(BLEPacket data) {
        synchronized (packageQueue) {
            packageQueue.remove(data);
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        d("PackageSets run..." + Thread.currentThread().getId());
        while (!isInterrupted()) {
            if (packageQueue.size() > 0) {
                byte[] data = packageQueue.get(0).bleData;
                d("dispatch : " + BleLibByteUtil.BytesToHexStringPrintf(data));

                dispatchPackage(packageQueue.get(0));
                removePackageData(packageQueue.get(0));
//				LogUtil.w(TAG,"PackageSets  removePackageData: " );
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
        d("PackageSets thread over");
    }

    synchronized void startRun() {
        if (isStart) {
            d("disp 已经运行了");
            return;
        }
        isStart = true;
        start();
        d("启动蓝牙数据分发线程。。。");
    }
    void stopRun() {
        d("stopRun PackageSets Thread");
        interrupt();
        isStart = false;
        packageQueue.clear();
    }

    private void d(String msg) {
//        Log.w(TAG, msg);
        String TAG = "BLEAllDispatcher";
        System.out.println(TAG + " " + msg);
    }

    private void dispatchPackage(BLEPacket message) {
        mBLEServerListener.onDeviceRespSpliceData(message);
    }

    public void dispatchOriginalPackage(byte[] data, int id) {
        BLEPacket message = new BLEPacket(data, id);
        mBLEServerListener.onDevicesRespOriginalData(message);
    }
}
