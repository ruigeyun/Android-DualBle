/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

public abstract class DataTransferManager {

//    private static DataTransferManager instance = new DataTransferManager();
//    private DataTransferManager(){}
//
//    public static DataTransferManager getInstance() {
//        return instance;
//    }

    public void initDataTransfer(OnDataSendCallBack callBack, DecodeAdapter adapter, NotifyCmdAdapter adapter2) {
        RecvCallbackManager.get().setCallBack(callBack);
        Decode.get().setDecodeAdapter(adapter);
        NotifyCmdLib.get().setAdapter(adapter2);
    }

    public void sendConfigCommand(OnRespDataListener l, byte[] values, int cmdId, int deviceId) {

        RecvCallbackManager.get().sendConfigCommand(l, values, cmdId, deviceId);
    }

    public void sendWriteCommand(OnRespDataListener l, byte[] values, int cmdId, int deviceId) {

        RecvCallbackManager.get().sendWriteCommand(l, values, cmdId, deviceId);
    }

    public void DecodeRespData(byte[] data, int deviceId) {
        RecvCallbackManager.get().parseRecvPackageData(data, deviceId);
    }

    public int parseTransmitData(byte[] data, CmdBase cmd) {

        return Decode.get().parseTransmitData(data, cmd);
    }

    public int parseConfigData(byte[] data, CmdBase cmd) {
        return Decode.get().parseConfigData(data, cmd);
    }

}
