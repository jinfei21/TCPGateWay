package com.ctrip.gateway.common;

import io.netty.buffer.ByteBuf;

public class GateMessage {
	private byte[] lenBytes;
	private byte[] dataBytes;
	private long createTime = System.currentTimeMillis();
	private int len;
	
	public void setDataBytes(byte[] data){
		this.dataBytes = data;
	}
	
	public void setLenBytes(byte[] data){
		this.lenBytes = data;
	}
	
	public void setCreateTime(long time){
		this.createTime = time;
	}
	
	public void setLen(int len){
		this.len = len;
	}
	
	public int getLen(){
		return this.len;
	}
	public void encodeToBuf(ByteBuf buf){
		buf.writeBytes(lenBytes);
		buf.writeBytes(dataBytes);
	}

	public byte[] getLenBytes() {
		return lenBytes;
	}

	public byte[] getDataBytes() {
		return dataBytes;
	}
	
	
}
