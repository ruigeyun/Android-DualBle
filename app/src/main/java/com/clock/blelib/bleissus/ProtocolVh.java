/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.bleissus;

public class ProtocolVh {

    public static String TAG = "ProtocolVh";

    public static final boolean enableCMD81Test = true; //81指令多出一个帧号，不算校验和内

    /************************** 包的基本框架数据 ********************************/
    // 标识位
    public static final int SYNC_FLAG_HEADER = 0xAA;
    // 命令字
    // 内容长度
    // 内容数据 n byte
    // 校验码
    public static final int SYNC_FLAG_END = 0xAA;

    public static final int INDEX_CHECK_CODE = 0x01; // 校验码的位置
    public static final int INDEX_CMD = 0x01;  // 命令类型的位置
    public static final int DIST_FLAG_2_LEN = 0x02;  // 包标志到包长度的距离
    public static final int EXTRA_DATA_CNT = 0x03;  // 包头后面的数据中，除了包内容，还有多少个其他字节（校验（1字节）+ 命令字（1字节） + 长度（1字节））
    //    public static final int EXTRA_DATA_CNT = 0x04;  // 包头后面的数据中，除了包内容，还有多少个其他字节（校验（1字节）+ 命令字（1字节） + 长度（1字节）+ 侦序号（1字节））临时添加

    public static final int INDEX_CONTENT = 1 + DIST_FLAG_2_LEN;

    /************************** 命令类型 ********************************/

    public static final byte COMMAND_LOGIN = 0X00;

    //  命令字（1字节） + 长度（1字节）
    public static final int LED_EXTRA_DATA_CNT = 0x02;

    public static final int CMD_PWD = 0x71;

    public static final int CMD_SET_POWER2 = 0x0d;
    public static final int CMD_SET_Refill = 0x0b;
    public static final int CMD_SET_Spray = 0x08;
    public static final int CMD_SET_Timer_POWER = 0x0e;
    public static final int CMD_SET_Interval = 0x0f;

    public static final int CMD_Query_POWER = 0x10;
    public static final int CMD_Query_Interval = 0x04;
    public static final int CMD_Query_Timer_power = 0x06;
    public static final int CMD_Query_Voltage = 0x02;
    public static final int CMD_Query_Refill = 0x09;

    public static final int CMD_Report_Voltage = 0x03;
    public static final int CMD_Report_Interval = 0x05;
    public static final int CMD_Report_Timer_power = 0x07;
    public static final int CMD_Report_Refill = 0x0a;
    public static final int CMD_Report_POWER = 0x0c;

    public static byte[] getCommand(byte comId, String command) {
        byte[] commandBytes = command.getBytes();
        byte[] commandFinal = new byte[2 + commandBytes.length];
        commandFinal[0] = (byte) (2 + commandBytes.length);
        commandFinal[1] = comId;
        System.arraycopy(commandBytes, 0, commandFinal, 2, commandBytes.length);
//        myLog.d("commandFinal: " + HexUtil.encodeHexStr(commandFinal));
        return commandFinal;
    }

}
