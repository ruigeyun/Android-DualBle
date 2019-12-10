/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.data;

/**
 * 数据解析适配器
 * 数据包结构必须是：包标识（1字节）+ 其他（n字节）+ 内容长度（1字节）+内容（n字节）+其他（n字节）
 * // aa     0a   07      00 14 00 00 00 00 00
 * // 包标志  指令 内容长度 内容
 */
public abstract class DataParserAdapter {
    public DataParserAdapter() {
    }
    /**
     * 包标志
     * 例子中：为 aa
     * @return
     */
    public abstract byte getPackageMark();
    /**
     * 包数据中，除了包标识、包内容，其他数据的总个数(命令字、校验值、预留数据等)
     * 例子中： 命令字（1字节） + 长度（1字节）0a 07  为2
     * @return
     */
    public abstract int getExtraDataNum();
    /**
     * 包标识 到 内容 间的距离，找到内容长的位置
     * 例子中：aa     0a   07  距离为2
     * @return
     */
    public abstract int getDistMarkToContent();
    /**
     * 获取 包头 到 包尾 的长度，不包括包头
     * contentLen  根据内容长度，获得包头 到 包尾 的长度
     * 数据包符合要求的，不需要重写这个方法
     * @return
     */
    public int getLenPackageHeadToTail(int contentLen) {
        return contentLen + getExtraDataNum();
    }
    /**
     * 获取一整包数据的长度
     * contentLen  根据内容长度，获取一整包数据的长度
     * 数据包符合要求的，不需要重写这个方法
     * @return
     */
    public int getLenOnePackage(int contentLen) {
        return contentLen + getExtraDataNum() + 1;
    }
}
