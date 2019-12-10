/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

class RecvCallbackManager {

    private final String TAG = RecvCallbackManager.class.getSimpleName();
    private static final long TIMEOUT = 3000;
    private static final int MSG_TIMEOUT = 1;
    private static final int MSG_COMMAND = 2;

    private ConcurrentHashMap<Integer, OnRespDataListener> businessCacheWaitResp = new ConcurrentHashMap<>();
    private byte[] timeoutArray;

    private static RecvCallbackManager instance = new RecvCallbackManager();

    private OnDataSendCallBack callBack;

    private RecvCallbackManager() {
        timeoutArray = getTimeoutRespMsg();
    }

    public static RecvCallbackManager get() {
        return instance;
    }

    void setCallBack(OnDataSendCallBack callBack) {
        this.callBack = callBack;
    }

    private byte[] getTimeoutRespMsg() {
        return new byte[]{(byte) 0xaa, (byte) 0xee, (byte) 0x00};
    }

    private void sendMessageDelayed(int token) {
        Message message = handler.obtainMessage(token, MSG_TIMEOUT, 0, timeoutArray);
        handler.sendMessageDelayed(message, TIMEOUT);
    }

    private void sendMessage(int token, byte[] src) {
        Message message = handler.obtainMessage(token, MSG_COMMAND, 0, src);
        handler.sendMessage(message);
    }

    void parseRecvPackageData(byte[] data, int bleId) {

        byte[] src = Decode.get().parseRecvData(data, bleId);
        if (src == null) {
            return;
        }

        int token = src[Decode.get().getDecodeAdapter().getIndexCmd()] & 0xFF;
        token = token + (bleId<<8);
		Log.w(TAG, "token 1 : " + token /*String.format("%02x", token)*/);
        sendMessage(token, src);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            final int token = msg.what;
            final byte[] message = (byte[]) msg.obj;

            switch (msg.arg1) {
                case MSG_COMMAND:
                    final OnRespDataListener response = businessCacheWaitResp.get(token);
                    if (response != null) {
                        removeWaitRespCache(token);
                        removeMessages(token); // 删除超时队列
                        response.onResponse(message);
                    } else {
                        Log.e(TAG, "!!!!!!!!!!!! get(token) null ： " + token);
                    }
                    break;

                case MSG_TIMEOUT:
                    final OnRespDataListener responseTimeout = businessCacheWaitResp.get(token);
                    if (responseTimeout != null) {
                        removeWaitRespCache(token);
                        Log.w(TAG, "remove(token): " + token);
                        responseTimeout.onResponse(message);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void addToWaitRespCache(int token, OnRespDataListener l) {
        businessCacheWaitResp.put(token, l);
    }

    private void removeWaitRespCache(int token) {
        businessCacheWaitResp.remove(token);
    }

    private void setReqTimeout(int token) {
        sendMessageDelayed(token);
    }

    /**
     * 回应的数据，直接回调，不走环形缓冲，因为这些数据没有包头。
     * 发送一条，回调结束后，才能再发下一条，否则回调方法会被新的一条覆盖掉
     * 没有超时检测
     *
     */
    void sendConfigCommand(OnRespDataListener l, byte[] values, int cmdId, int bleId) {

//        BLEManager.getInstance().sendBLEConfigData(values, bleId);
        callBack.onSendData(values, bleId);
    }

    void sendWriteCommand(OnRespDataListener l, byte[] values, int cmdId, int bleId) {

        sendDataLogic(l, values, cmdId, bleId);
    }

    private void sendDataLogic(OnRespDataListener l, byte[] values, int cmdId, int bleId) {

        Log.w(TAG, "token 0 : " + cmdId /*String.format("%02x", cmdId)*/);
        addToWaitRespCache(cmdId, l); // 添加响应回调到缓存，等待回调

        sendBytesDate(values, bleId);              // 发送数据

        setReqTimeout(cmdId);          // 超时处理
    }

    private void sendBytesDate(final byte[] data, int bleId) {
        //一条包字节大于20个，要手动分包
        if(data==null){
            return;
        }
        byte[] sendBytes;
        sendBytes = data;
        int send_time = sendBytes.length / 20;
        if (send_time >= 1) {
            for (int i = 0; i < send_time; i++) {
                byte[] sendByte = new byte[20];
                for (int j = 0; j < 20; j++) {
                    sendByte[j] = sendBytes[20 * i + j];
                }
//                BLEManager.getInstance().sendBLETransmitData(sendByte, bleId);
                callBack.onSendData(sendByte, bleId);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (sendBytes.length % 20 > 0) {
                byte[] sendByte = new byte[sendBytes.length % 20];
                for (int i = 0; i < sendBytes.length % 20; i++) {
                    sendByte[i] = sendBytes[sendBytes.length - sendBytes.length % 20 + i];
                }
//                BLEManager.getInstance().sendBLETransmitData(sendByte, bleId);
                callBack.onSendData(sendByte, bleId);
            }
        } else {
//            BLEManager.getInstance().sendBLETransmitData(data, bleId);
            callBack.onSendData(data, bleId);
        }
    }
}
