/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel.led;


import com.clock.blelib.bleissus.ProtocolVh;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

public class LedDataAdapter extends DataParserAdapter {

    public LedDataAdapter() {}
    /**
     * 包标志
     * @return
     */
    @Override
    public byte getPackageMark() {
        return (byte) ProtocolVh.SYNC_FLAG_HEADER;
    }
    /**
     * 包数据中，除了包标识，包内容，的其他数据的总长度
     * @return
     */
    @Override
    public int getExtraDataNum() {
        //  命令字（1字节） + 长度（1字节）
        return ProtocolVh.LED_EXTRA_DATA_CNT;
    }
    /**
     * 包标识 到 内容 间的距离，找到内容长的位置
     * @return
     */
    @Override
    public int getDistMarkToContent() {
        return ProtocolVh.DIST_FLAG_2_LEN;
    }
    /**
     * 获取 包头 到 包尾 的长度，不包括包头
     * contentLen  根据内容长度，获得包头 到 包尾 的长度
     * @return
     */
    @Override
    public int getLenPackageHeadToTail(int contentLen) {
        return contentLen - 1;
    }
    /**
     * 获取一整包数据的长度
     * contentLen  根据内容长度，获取一整包数据的长度
     * @return
     */
    @Override
    public int getLenOnePackage(int contentLen) {
        return contentLen;
    }
}
