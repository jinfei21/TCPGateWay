package com.ctrip.gateway.common;

public class HeartBeatRequest extends GateRequest{

	public HeartBeatRequest(){
		setRequestType(MessageType.HEARTBEAT);
	}
}
