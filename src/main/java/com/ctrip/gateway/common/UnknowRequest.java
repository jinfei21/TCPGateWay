package com.ctrip.gateway.common;


public class UnknowRequest extends GateRequest {

	public UnknowRequest(){
		setRequestType(MessageType.UNKNOW);
	}
	
}
