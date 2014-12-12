package com.ctrip.gateway.client;

import com.ctrip.gateway.common.GateRequest;
import com.ctrip.gateway.common.GateResponse;

public interface GateClient {
	GateResponse request(String host,int port,GateRequest request)throws Exception;
	GateResponse request(String host,int port,GateRequest request,int timeout)throws Exception;
}
