/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network;

public interface DeviceAuthorizeListener {
    /**
     * 校验通过，继续往下走，校验失败，删除设备
     * @param isAuthorize true  校验通过， false 校验失败
     */
    public void onAuthorize(boolean isAuthorize);
}
