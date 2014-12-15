package com.ctrip.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.net.GateNettyServer;

public class TCPGateWayServer extends AbstractServer{
	private static final Logger logger = LoggerFactory.getLogger(TCPGateWayServer.class);
	private GateNettyServer gateServer;
	
	public TCPGateWayServer() throws Exception{
		super();
		init();
	}

	@Override
	protected void init() throws Exception {
		logger.info("start TCPGateWayServer...");
		
		
		this.gateServer = new GateNettyServer();
	}

	@Override
	protected void doStart() throws Exception {

		
	}

	@Override
	protected void doClose() throws Exception {


		
	}

	
}
