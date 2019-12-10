/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;


import android.util.Log;

class Decode {

	private final String TAG = Decode.class.getSimpleName();
	
	private static Decode instance = new Decode();
	private Decode() {};
	public static Decode get() {
		return instance;
	}

	private DecodeAdapter decodeAdapter;
	void setDecodeAdapter(DecodeAdapter adapter) {
		decodeAdapter = adapter;
	}
	DecodeAdapter getDecodeAdapter() {
		return decodeAdapter;
	}

	private boolean calcCheckCode(byte[] data) {
		int len = data.length-1;
		int result = 0;
		for(int index=4; index<len; index++) {
			result += data[index];
		}
		Log.d(TAG, "decode CheckCode: " + result);
		byte temp = (byte) (result & 0xFF);
		Log.d(TAG, "decode  CheckCode byte: " + temp);
		Log.d(TAG, "src checkout: " + data[decodeAdapter.getIndexCheckCode()]);
		if(temp == data[decodeAdapter.getIndexCheckCode()]) {
			return true;
		}
		return false;
	}

	byte[] parseRecvData(byte[] data, int bleId) {
		// 0、字符个数太少
//		if(data.length < ProtocolVh.MIN_LENGHT) {
//			Log.w(TAG, "数据长度不够，错误数据");
//			return null;
//		}
		int cmd = data[decodeAdapter.getIndexCmd()] & 0xFF;
//		Log.i(TAG, "Cmd: " + String.format("%02x", cmd));

		if(parseNotifyCmd(data, cmd, 0, bleId) == -1) { // 不属于通知指令，继续往下解析
			return data;
		}
		return null;
	}
	
	private int parseNotifyCmd(byte[] data, int currCmd, int index, int bleId) {

		String className = NotifyCmdLib.get().getClassString(currCmd);
		if(className == null) {
//			Log.i(TAG, "不是通知指令: " + currCmd);
			return -1;
		}
		Log.i(TAG, "通知指令 className: " + className);

		CmdInterface desCmd = NotifyCmdLib.get().getObjectFromLib(currCmd);
		if(desCmd == null) {
			try {
				Log.w(TAG, "ObjectLib get null 通过反射创建实例");
				desCmd = (CmdInterface) Class.forName(className).newInstance();
				NotifyCmdLib.get().putObjectToLib(currCmd, desCmd);
			} catch (Exception e) {
				e.printStackTrace();
				return index;
			}
		}

		int len = getContentLen(data, index);
        index = 0 + decodeAdapter.getDistMarkToContent() + 1;
		desCmd.parseNotifyCmd(data, index, len, bleId);

		return index + len;
	}

	public int parseTransmitData(byte[] data, CmdBase cmd) {

		int len = getContentLen(data, 0);
		int index = 0 + decodeAdapter.getDistMarkToContent() + 1;
		return cmd.parseTransmitCmd(data, index, len);
	}

	public int parseConfigData(byte[] data, CmdBase cmd) {

		int len = data[0] & 0xFF;
		int index = 0;
		return cmd.parseConfigCmd(data, index, len);
	}

	private int getContentLen(byte[] data, int index) {
		int len = data[(index+ decodeAdapter.getDistMarkToContent())] & 0xFF;
//		len = len << 8;
//		len = len | (data[index++] & 0xFF);
		return len;
	}
	
}
