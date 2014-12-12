package com.ctrip.gateway.common;


public class UnknowResponse extends GateResponse {

	public UnknowResponse(){
		setResponseType(MessageType.UNKNOW);
	}
	
}
