/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

/**
 * 命令模式 
 * @author clock
 *
 */
public interface CmdInterface {
	/**
	 *
	 * @param data 一包数据
	 * @param index 命令的内容部分开始的位置
	 * @param len 命令内容部分的长度
	 * @return
	 */
	public int parseNotifyCmd(byte[] data, int index, int len, int bleId);
	public int parseTransmitCmd(byte[] data, int index, int len);
	public int parseConfigCmd(byte[] data, int index, int len);
	public boolean packDataForSend();
}
