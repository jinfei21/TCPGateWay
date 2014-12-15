package com.ctrip.gateway.client;

import static com.ctrip.gateway.common.Constant.*;

public class GateConnectionFactory {

	private String host;
	private int port;
	private int connectTimeout;
	private int readTimeout;
	
	public GateConnectionFactory(String host,int port,int connectTimeout,int readTimeout){
		this.host = host;
		this.port = port;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}
	
	public GateConnectionFactory(String host,int port){
		this(host, port,DEFAULE_CONNECT_TIMEOUT,DEFAULE_READ_TIMEOUT);
	}
	
	public GateConnection createConnection(){
		return new SocketGateConnection(host,port,connectTimeout,readTimeout);
	}
	
}
