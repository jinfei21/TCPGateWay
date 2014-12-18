package com.ctrip.gateway.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;

public class GateMessage {
	private byte[] lenBytes;
	private byte[] dataBytes;
	private long createTime = System.currentTimeMillis();
	private int len;
	
    private Map<String, Object> headers = new ConcurrentHashMap<String, Object>();
    
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
	
    public Object getHeader(String name) {
        return headers.get(name);
    }

    public Object getHeader(String name, Object defaultValue) {
        Object o = headers.get(name);
        return o == null ? defaultValue : o;
    }

    public Object setHeader(String name, Object val) {
        return headers.put(name, val);
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
}
