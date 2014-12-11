package com.ctrip.gateway.common;

public class BusinessRequest extends GateRequest{

	public BusinessRequest(){
		setRequestType(MessageType.BUSINESS);
	}
}
