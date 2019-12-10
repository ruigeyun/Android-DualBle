/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

public enum DeviceState {

    Normal(0), // normal 状态的设备，才可以被连接
    Remove(1), // 被删除
    DisConnRelease(2);// 被断开，在释放资源状态，此状态下，不可以被连接

    // 成员变量
    private int index;
    // 构造方法
    private DeviceState(int index) {
        this.index = index;
    }

}
