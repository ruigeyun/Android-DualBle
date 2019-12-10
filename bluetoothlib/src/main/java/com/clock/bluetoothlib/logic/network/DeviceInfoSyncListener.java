/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network;

public interface DeviceInfoSyncListener {

    /**
     * 同步结束后，调用此接口，设备进入正式使用状态
     * @param type 目前没有意义
     */
    public void onInfoSync(int type);

}
