package com.ctrip.gateway;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.ctrip.gateway.common.Constant.*;
import com.ctrip.gateway.common.GateFilterManage;
import com.ctrip.gateway.net.GateNettyServer;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

public class TCPGateWayServer extends AbstractServer {
	private static final Logger logger = LoggerFactory
			.getLogger(TCPGateWayServer.class);
	private GateNettyServer gateServer;

	public TCPGateWayServer() throws Exception {
		super();
		init();
	}

	@Override
	protected void init() throws Exception {
		logger.info("start TCPGateWayServer...");
		final DynamicIntProperty freshInterval = DynamicPropertyFactory.getInstance().getIntProperty(FILTER_FRESH_INTERVAL, 5);
		freshInterval.addCallback(new Runnable() {
	
			public void run() {
				GateFilterManage.setRefreshInterval(freshInterval.get());
			}
		});
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        String pre_route = config.getString(PREV_ROUTE_PATH);
        String proc_route = config.getString(PROC_ROUTE_PATH);
        String post_route = config.getString(POST_ROUTE_PATH);
   
		GateFilterManage.instance().init(freshInterval.get(), pre_route,post_route,post_route);
		this.gateServer = new GateNettyServer();
	}

	@Override
	protected void doStart() throws Exception {

	}

	@Override
	protected void doClose() throws Exception {
		gateServer.close();
	}

}
