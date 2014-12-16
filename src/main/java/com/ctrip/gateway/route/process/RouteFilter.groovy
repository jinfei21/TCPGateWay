package com.ctrip.gateway.route.process;

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ctrip.gateway.client.DefaultGateClient
import com.ctrip.gateway.client.GateClient
import com.ctrip.gateway.common.BaseIsolationCommand
import com.ctrip.gateway.common.GateFilter
import com.ctrip.gateway.common.GateRequest
import com.netflix.config.DynamicPropertyFactory
import com.netflix.config.DynamicStringProperty

public class RouteFilter extends GateFilter{

	private static final Logger logger = LoggerFactory.getLogger(RouteFilter.class);
    private DynamicStringProperty routeAddr = DynamicPropertyFactory.getInstance().getStringProperty("tcp.route.address", null);

	private DefaultGateClient client;
	
    public RouteFilter(){
		client = new DefaultGateClient();
    }
    
	@Override
	public String filterType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int filterOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean shouldFilter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object run() {
        String host
        int port
        try {
            String[] parts = routeTarget.split(":")
            host = parts[0]
            port = Integer.parseInt(parts[1].trim())
        } catch (Exception e) {
            throw new Exception(e, "Tcp Mobile Address is illegal", 500, "GATE-Illegal-Config-TcpMobile",)
        }
        
		return null;
	}
    
	@Override
	protected void finalize() throws Throwable {
		logger.warn("Finalize before GC.")
		logger.warn("Release all connections while finalizing.")
		client.releaseAllConnections();
		logger.warn("Has finalized before GC.")
	}
    
}


class SemaphoreIsolationCommand extends BaseIsolationCommand{
	    private static Logger logger = LoggerFactory.getLogger(SemaphoreIsolationCommand.class)
	
		private GateClient client;
		private GateRequest request
		private String host
		private int port
		private int readTimeout
	
	public SemaphoreIsolationCommand(GateClient client, String host, int port, GateRequest request, String commandGroup, String commandKey, int readTimeout){
		super(commandGroup, commandKey);
		this.client = client;
		this.host = host;
		this.port = port;
		this.request = request;
		this.readTimeout = readTimeout;
	}

	@Override
	protected Object run() throws Exception {
        try {
            return client.request(host, port, request, readTimeout)
        } catch (Exception e) {
            //Try again if connection reset or broken pipe
            String message = e.getMessage()
            if ("Connection reset".equals(message)
                || "Connection is closed".equals(message)
                || "Broken pipe".equals(message)) {
                logger.warn("There is a socket exception, would retry.", e)
                return client.request(host, port, request, readTimeout)
            }
            throw e
        }
	}
	
}
