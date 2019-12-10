/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.network.connection;

public final class BLEState {

    public enum Scan {
        ScanIdle(0), // 系统还没准备好，初始状态
        CanScan(1),  // 可以开始扫描
        Scanning(2), // 正在扫描
        Scanned(3); // 扫描结束了

         private int index;
         private Scan(int index) {
            this.index = index;
        }
    }

    public enum Connect {
        ConnectIdle(0),  //
        ConnectFail(2),
        Disconnect(3),  //
        BeToDisconnect(4), // 当前设备被其他设备挤下去，准备进入断开状态
        ForceDisconnect(5),  // 被强制断开连接
        InitiativeConnect(6),  // 主动去连

        Connecting(10),   //
        Connected(12),   //

        Enable(20), // 特性配置成功，读写方式配置完成，蓝牙设备才真正能通讯
        Authorize(50),// 授权通过的设备，就可以交互业务数据了
        Active(100);  // 基础数据通讯配置完成，进入正常业务流程

        public int index;
        private Connect(int index) {
            this.index = index;
        }
    }

    public enum Reconnect {
        ReConnIdle(0),
        Reconnecting(1);   //

        // 成员变量
        private int index;
        // 构造方法
        private Reconnect(int index) {
            this.index = index;
        }
    }

    public enum ReconnectType {
        Auto(0),
        Manual(1);   //

        // 成员变量
        private int index;
        // 构造方法
        private ReconnectType(int index) {
            this.index = index;
        }
    }

}
