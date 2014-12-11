package com.ctrip.gateway.common;

import java.net.InetAddress;

public class GateRequest extends GateMessage{

	private long completedReceiveTime;
	private MessageType requestType;	
	private InetAddress localAddress;
	private int localPort;
	
	private InetAddress remoteAddress;
	private int remotePort;
	
	
	public long getCompletedReceiveTime() {
		return completedReceiveTime;
	}
	public void setCompletedReceiveTime(long completedReceiveTime) {
		this.completedReceiveTime = completedReceiveTime;
	}
	public MessageType getRequestType() {
		return requestType;
	}
	public void setRequestType(MessageType requestType) {
		this.requestType = requestType;
	}
	public InetAddress getLocalAddress() {
		return localAddress;
	}
	public void setLocalAddress(InetAddress localAddress) {
		this.localAddress = localAddress;
	}
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

}
