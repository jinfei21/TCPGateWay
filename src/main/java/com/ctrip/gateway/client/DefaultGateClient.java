package com.ctrip.gateway.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.common.GateRequest;
import com.ctrip.gateway.common.GateResponse;

public class DefaultGateClient implements GateClient {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultGateClient.class);
	
	
	
	
	public DefaultGateClient(){
		
	}

	public GateResponse request(String host, int port, GateRequest request)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public GateResponse request(String host, int port, GateRequest request,
			int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
