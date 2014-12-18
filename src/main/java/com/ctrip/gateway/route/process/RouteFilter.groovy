package com.ctrip.gateway.route.process;

import static com.ctrip.gateway.common.Constant.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ctrip.gateway.client.DefaultGateClient
import com.ctrip.gateway.client.GateClient
import com.ctrip.gateway.common.BaseIsolationCommand
import com.ctrip.gateway.common.GateFilter
import com.ctrip.gateway.common.GateRequest
import com.ctrip.gateway.common.GateResponse
import com.ctrip.gateway.common.RequestContext

public class RouteFilter extends GateFilter{

	private static final Logger logger = LoggerFactory.getLogger(RouteFilter.class);

	private DefaultGateClient client;
	
    public RouteFilter(){
		client = new DefaultGateClient();
    }
    
	@Override
	public String filterType() {
		return PROC;
	}

	@Override
	public int filterOrder() {
		return 20;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		
		RequestContext context = RequestContext.currentContext();
		GateRequest gateRequest = context.getOriginRequest();
		
		String routeAddress = context.getRouteAddress();
        String host;
        int port;
        try {
			if(routeAddress==null||"none".equals(routeAddress)){
				context.setGateResponse(null);
			}else{
	            String[] parts = routeAddress.split(":");
	            host = parts[0];
	            port = Integer.parseInt(parts[1].trim());
				GateResponse response = new SemaphoreIsolationCommand(client, host, port, gateRequest, "group", "key", 1000*12).execute();
				context.setGateResponse(response);
			}
        } catch (Exception e) {
           logger.error("route error,routeAddress:"+routeAddress, e);
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
