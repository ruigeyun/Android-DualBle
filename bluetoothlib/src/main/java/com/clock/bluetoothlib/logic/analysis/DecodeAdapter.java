/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

public abstract class DecodeAdapter {
    public DecodeAdapter() {
    }
    /**
     * 命令字节的位置
     * @return
     */
    public abstract byte getIndexCmd();
    /**
     * 校验码的位置
     * @return
     */
    public abstract int getIndexCheckCode();
    /**
     * 包标识 到 内容 间的距离，找到内容长的位置
     * @return
     */
    public abstract int getDistMarkToContent();
}
