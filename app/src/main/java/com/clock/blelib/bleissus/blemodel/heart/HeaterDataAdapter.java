/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus.blemodel.heart;

import com.clock.blelib.bleissus.ProtocolVh;
import com.clock.bluetoothlib.logic.network.data.DataParserAdapter;

public class HeaterDataAdapter extends DataParserAdapter {
    // aa     0a   07      00 14 00 00 00 00 00
    // 包标志  指令 内容长度 内容
    public HeaterDataAdapter() {}
    /**
     * 包标志
     * @return
     */
    @Override
    public byte getPackageMark() {
        // aa
        return (byte) ProtocolVh.SYNC_FLAG_HEADER;
    }
    /**
     * 包数据中，除了包标识，包内容，的其他数据的总长度
     * @return
     */
    @Override
    public int getExtraDataNum() {
        //  命令字（1字节） + 长度（1字节）0a 07
        return ProtocolVh.LED_EXTRA_DATA_CNT;
    }
    /**
     * 包标识 到 内容 间的距离，找到内容长的位置
     * @return
     */
    @Override
    public int getDistMarkToContent() {
        // aa     0a   07 距离为2
        return ProtocolVh.DIST_FLAG_2_LEN;
    }
}
