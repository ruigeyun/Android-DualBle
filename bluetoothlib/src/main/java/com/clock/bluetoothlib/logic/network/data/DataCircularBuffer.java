/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.data;

import android.util.Log;


import com.clock.bluetoothlib.logic.network.BleLibByteUtil;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据环形缓冲器
 * 数据包结构必须是：包标识（1字节）+ 其他（n字节）+ 内容长度（1字节）+内容（n字节）+其他（n字节）
 * 此算法，根据包标志、包长度、包结构，挑出一个完整数据包
 */
public final class DataCircularBuffer {
    private final String TAG2 = "CircularBufferBase";
    private byte[] buffer;
    private int readPos;
    private int writePos;
    private int capacity;      // 容量
    private int readableSize; // 当前可读元素个数
    private LinkedList<byte[]> packageQueue = new LinkedList<byte[]>();
    private BLEAllDispatcher mDataDispatchCenter = null;
    private byte[] onePackage;

    private int bleId;
    private DataParserAdapter adapter;

    public DataCircularBuffer(int cap, int id, DataParserAdapter adapter) {
        bleId = id;
        mDataDispatchCenter = BLEAllDispatcher.getInstance();
        this.adapter = adapter;
        if (this.adapter == null) {
            d("没有数据解析适配器，不用初始化缓冲区");
            return;
        }
        buffer = new byte[cap];
        readPos = writePos = 0;
        readableSize = 0;
        capacity = cap;
        d("BLELedBuffer capacity = " + capacity);
        mDataDispatchCenter.startRun();
    }

    public void destroyBuffer() {
        reset();
        packageQueue = null;
        buffer = null;
        capacity = 0;
        mDataDispatchCenter = null;
//        mDataDispatchCenter.stopRun();
    }

    void reset() {
        readPos = 0;
        writePos = 0;
        readableSize = 0;
        packageQueue.clear();
    }

    private void printfBuffer() {
        Log.e(TAG2, "缓冲区所有数据 从缓冲头开始：" + BleLibByteUtil.BytesToHexStringPrintf(buffer));
        int index = readPos;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < capacity; i++) {
            if (index >= capacity) {
                builder.append(String.format("%02x", buffer[(index - capacity)]) + " ");
            } else {
                builder.append(String.format("%02x", buffer[index]) + " ");
            }
            index++;
        }
        Log.e(TAG2, "缓冲区所有数据 从读取位开始：" + builder.toString());
    }

    /**
     * @param len
     * @return 写入成功返回0，其他-1
     */
    private int writeArray(byte[] des, int len) {
        if (!isSpaceEnough(len)) {
            return -1;
        }
        //		d("写之前的size: " + readableSize + " writePos: " + writePos);
        for (int i = 0; i < len; i++) {
            writeByte(des[i]);
        }
        //		d("写后的size: " + readableSize + " writePos: " + writePos);
        return 0;
    }

    private void writeByte(byte a) {
        if (writePos == capacity) {
            //			Log.w(TAG2,"写了一圈，从头开始");
            writePos = 0;
            d("writePos set 0");
        }
        buffer[writePos++] = a;
        // buf[writePos++ % capacity] = a;
        ++readableSize;
    }

    /**
     * 从缓冲区读走len长的数据
     * @param len
     * @return
     */
    private byte[] readArray(int len, byte mark) {
        if (IsEmpty() || len > capacity || len > readableSize) {
            d("ReadArray error!");
            return null;
        }
        //		d("读之前的readableSize: " + readableSize + " readPos: " + readPos);
        byte[] temp = new byte[len];
        for (int i = 0; i < len; i++) {
            temp[i] = readByte();
        }
        //		d("读后的readableSize: " + readableSize + " readPos: " + readPos);
//        Log.v(TAG2, "readArray: " + BytesUtil.BytesToHexStringPrintf(temp));
        if (temp[0] != mark) {
            Log.e(TAG2, "数据第一个字节不是正确的头: " + BleLibByteUtil.BytesToHexStringPrintf(temp));
            int j = 0;
            for (j = 0; j < len; j++) {
                if (temp[j] == mark) {
                    break;
                }
            }
            Log.i(TAG2, "size: " + temp.length + " len:" + len + " j: " + j);
            byte[] temp2 = Arrays.copyOfRange(temp, j, len); // 不包含最后一个字节，不用len-1
            Log.e(TAG2, "截取后的数组：" + BleLibByteUtil.BytesToHexStringPrintf(temp2));
            return temp2;
        }
        return temp;
    }

    private byte readByte() {
        if (readPos == capacity) {
            //			Log.w(TAG2,"读一圈了，从头开始");
            readPos = 0;
            d("readPos set 0");
        }
        --readableSize;
        return buffer[readPos++];
    }

    private boolean IsEmpty() {
        if (readableSize == 0) {
            d("IsEmpty!");
            return true;
        }
        return false;
    }

    private int getWriteAndReadDis() {
        if (writePos < readPos) {
            return ((capacity + writePos) - readPos);
        } else {
            return (writePos - readPos);
        }
    }

    private int getDistCurrPosToEnd(int currPos) { // 必须同步加锁，单线程执行
        if (writePos < currPos) {
            return ((capacity + writePos) - currPos);
        } else {
            return (writePos - currPos);
        }
    }

    /**
     * 是否有足够的空间写入新数据
     *
     * @param writeLen
     * @return
     */
    private boolean isSpaceEnough(int writeLen) {
        if ((readableSize + writeLen) > capacity) {
            d("IsWriteEnough!");
            return false;
        }
        return true;
    }

    /**
     * 提取所有包
     * 缓冲区写入了很多数据，从这些数据中，把有效包一个个提取出来
     * 可能包含一个，可能多个，可能半个，可能多个加半个，可能一个也没有都是辣鸡数据
     *
     * @param mark 包标识
     * @return 返回有效包队列
     */
    private List<byte[]> extractAllPackage(byte mark) {
        packageQueue.clear(); // 如果数据还没处理完，又来新数据，就会把旧数据清空，可能导致错误，需同步，但是这种情况目前还没出现
        seekForPackage(mark);
        return packageQueue;
    }

    /**
     * 找寻符合包定义的数据段
     * 根据包标志，包长度，包结构，找到符合包定义数据段
     * @param mark 目标标志
     * @return
     */
    private int seekForPackage(byte mark) {
        int number = 0;
        int cnt = readableSize;
        int pos = readPos;
        int contentLen = 0;
        int packageLen = 0;

        for (int i = 0; i < cnt; i++) {
            if (pos >= capacity) {
                //				Log.w(TAG2,"已经读了一圈");
                pos = 0;
            }
            // 搜索到包标志
            if (buffer[pos] == mark) { // pos大于buff容量的判断，前两行代码已经判断了
                // 包头到最后面的数据的长度， 判断后面是否还有数据，携带包长度
                if (getDistCurrPosToEnd(pos) > adapter.getDistMarkToContent()) {
                    if (pos + adapter.getDistMarkToContent() >= capacity) { // 包标志跟长度值，在环形缓冲区的临界两边
                        contentLen = buffer[(pos + adapter.getDistMarkToContent() - capacity)] & 0xFF;
                        Log.w(TAG2, "长度index跨界: " + (pos + adapter.getDistMarkToContent() - capacity));
                    } else {
                        contentLen = buffer[pos + adapter.getDistMarkToContent()] & 0xFF;
                    }
//                    					Log.v(TAG2,"包len: " + contentLen);
                    if (/*contentLen < 0 || */contentLen > 0x20) { // 内容长度为负数，或太大，都是辣鸡数据
                        Log.w("buff", "内容长度异常: " + contentLen);
                        printfBuffer();
                        buffer[pos] = 0;// 把标志位抹掉
                        pos++;
                        continue;
                    }
                    // 判断后面的数据，是否足够一包
                    if (getDistCurrPosToEnd(pos) > adapter.getLenPackageHeadToTail(contentLen)) { // 包长度(此处不包含包标志):包内容的长度+ 命令字（1字节） + 长度（1字节）
                        packageLen = adapter.getLenOnePackage(contentLen)+ i; //（+1 表示加上包标志） i 表示垃圾数据的个数，检索到包标志前，有其他垃圾数据
//                        Log.v(TAG2,"packageLen: " + packageLen);
                        onePackage = pickOutOnePackageData(packageLen, readPos, mark);
                        if (true) {
                            //                        if(checkPackage(onePackage, contentLen) == 0) {
                            // 校验通过，读取这包数据，放到包队列
                            packageQueue.add(readArray(packageLen, mark));
                            pos = readPos;
                            cnt = readableSize;
                            i = -1; // 马上执行一次++
                            number++;
                            continue;
                        } else {
                            // 错误的包数据
                            Log.w("buff", "这包数据为辣鸡数据: " + BleLibByteUtil.BytesToHexStringPrintf(onePackage));
                            buffer[pos] = 0;// 把标志位抹掉
                        }
                    } else {
                        Log.w(TAG2, "不是完整包 后面的数据，不够一包: " + getDistCurrPosToEnd(pos));
                        return -3;
                    }
                } else {
                    Log.w(TAG2, "不是完整包 后面没有包长度: " + getDistCurrPosToEnd(pos));
                    return -2;
                }
            }
            pos++;
        }
        //		Log.w(TAG2,"number == " + number);
        if (number == 0) {
            Log.w(TAG2, "没有找到包标志");
            return -1;
        }
        return 0;
    }



    /**
     * 从缓冲区复制一包数据，主要用来检测这包数据是否有效
     *
     * @param len
     * @param index
     * @return
     */
    private byte[] pickOutOnePackageData(int len, int index, byte mark) {
        if (IsEmpty() || len > capacity || len > readableSize) {
            Log.e("buff", "pickOut ReadArray error： " + len);
            return null;
        }
        //        d("2 读之前的readableSize: " + readableSize + " readPos: " + readPos);
        byte[] temp = new byte[len];
        int pos = index;
        for (int i = 0; i < len; i++) {
            if (pos >= capacity) {
                pos = 0;
            }
            temp[i] = buffer[pos++];
        }
        //        d("2 读后的readableSize: " + readableSize + " readPos: " + readPos);
        //        Log.i("buff", "pickOut 找到包标志位 根据长度挑出一个包: " + BytesUtil.BytesToHexStringPrintf(temp));
        if (temp[0] != mark) {
            Log.w("buff", "pickOut 数据第一个字节不是正确的头: " + BleLibByteUtil.BytesToHexStringPrintf(temp));
            int j = 0;
            for (j = 0; j < len; j++) {
                if (temp[j] == mark) {
                    break;
                }
            }
            //            Log.i("buff", "pickOut size: " + temp.length + " len:" + len + " j: " + j);
            byte[] temp2 = Arrays.copyOfRange(temp, j, len); // 不包含最后一个字节，不用len-1
            Log.w("buff", "pickOut 截取后的数组：" + BleLibByteUtil.BytesToHexStringPrintf(temp2));
            return temp2;
        }
        return temp;
    }

    /**
     * 分析初始数据
     * 每个包只有一个标志位 0xAA 命令 长度 内容 校验
     * 读到标志位后，根据长度，把后面的读完
     *
     * @param data
     * @param length
     * @return
     */
    private int analyzeOriginalData(byte[] data, int length) {
        int ret = writeArray(data, length);
        if (ret == -1) {
            Log.w(TAG2, "writeArray 失败 缓冲器溢出 ");
            reset();
            return ret;
        }
        extractAllPackage(adapter.getPackageMark());

        if (packageQueue.size() == 0) {
            return -1;
        }

        mDataDispatchCenter.addAllPackageData(packageQueue, bleId);
        return ret;
    }

    private long prevTime = 0;
    private long currTime = 0;

    public void pushOriginalDataToBuffer(byte[] originalData) {
        LogUtil.v(TAG2, "ble发来的原始数据: " + BleLibByteUtil.BytesToHexStringPrintf(originalData));

        //        currTime = System.currentTimeMillis();
        //        Log.v(TAG2, "push currTime: " + currTime);
        //        Log.v(TAG2, "push 间隔: " + (currTime - prevTime));
        //        prevTime = currTime;
        // 没有配置数据拼包适配器，则把原始数据分发出去
        if (adapter == null) {
            mDataDispatchCenter.dispatchOriginalPackage(originalData, bleId);
        }
        else {
            // 此框架数据包拼包解析算法，数据包格式见：DataParserAdapter
            analyzeOriginalData(originalData, originalData.length);
        }

    }

    private void d(String msg) {
        Log.w("CircularBuffer", msg);
    }
}


