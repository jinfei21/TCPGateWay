package com.ctrip.gateway.common;

public class HeartBeatResponse extends GateResponse {

	public HeartBeatResponse(){
		setResponseType(MessageType.HEARTBEAT);
	}
}
