package com.ctrip.gateway.common;

public class BusinessResponse extends GateResponse{

	public BusinessResponse(){
		setResponseType(MessageType.BUSINESS);
	}
}
