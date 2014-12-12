package com.ctrip.gateway.client;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.common.GateRequest;
import com.ctrip.gateway.common.GateResponse;

public class DefaultGateClient implements GateClient {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultGateClient.class);
	
	private Map<String,GateConnectionPool> pool;
	
	public DefaultGateClient(){
		this.pool = new ConcurrentHashMap<String,GateConnectionPool>();
	}

	public GateResponse request(String host, int port, GateRequest request)throws Exception {
		
		
		return null;
	}

	public GateResponse request(String host, int port, GateRequest request,int timeout) throws Exception {


		
		return null;
	}

}
