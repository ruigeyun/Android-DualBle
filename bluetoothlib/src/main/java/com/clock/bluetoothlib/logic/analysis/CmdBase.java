/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

import java.util.Arrays;


public class CmdBase implements CmdInterface {
	protected String TAG = "CmdInterface";
	protected int id = 0;
	protected int result = -1;
	protected byte[] value = null;

	public CmdBase(){
		result = -1;
	};
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public byte[] getValue() {
		return value;
	}
	public void setValue(byte[] value) {
		this.value = value;
	}

	public int getValueLen() {
		if(value == null) {
			return 0;
		}
		else {
			return value.length;
		}
	}
	
	@Override
	public int parseNotifyCmd(byte[] data, int index, int len, int deviceId) {
		// TODO Auto-generated method stub
		if(data == null) {
			return ConstantTransfer.CODE_DATA_NULL;
		}
//		if(len == 0) {
//			return ProtocolVh.RESP_LEN_0;
//		}
		
		return ConstantTransfer.CODE_SUCCESS;
	}

	@Override
	public int parseTransmitCmd(byte[] data, int index, int len) {
		// TODO Auto-generated method stub
		if(data == null) {
			return ConstantTransfer.CODE_DATA_NULL;
		}
		// byte[]{(byte) 0xaa, (byte)0xee, (byte)0x00};
		if(len==0 && data.length==3 && data[1]==(byte)0xee) {
			return ConstantTransfer.CODE_TIMEOUT;
		}
//		if(len == 0) {
//			return ProtocolVh.RESP_LEN_0;
//		}
		return ConstantTransfer.CODE_SUCCESS;
	}

	@Override
	public int parseConfigCmd(byte[] data, int index, int len) {
		// TODO Auto-generated method stub
		if(data == null) {
			return ConstantTransfer.CODE_DATA_NULL;
		}
//		if(len == 0) {
//			return ProtocolVh.RESP_LEN_0;
//		}
		return ConstantTransfer.CODE_SUCCESS;
	}

	/**
	 * 如果发送的数据携带参数，必须实现这个方法，把数据打包
	 */
	@Override
	public boolean packDataForSend() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		return "CmdBase [id=" + id + ", result=" + result + ", value="
				+ Arrays.toString(value) + "]";
	}
}
