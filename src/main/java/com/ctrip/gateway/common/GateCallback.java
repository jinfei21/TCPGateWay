package com.ctrip.gateway.common;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateCallback implements Callable{

	private static final Logger logger = LoggerFactory.getLogger(GateCallback.class);
	
	private GateRequest request;
	private GateResponse response;
	
	public GateCallback(GateRequest request,GateResponse response){
		this.request = request;
		this.response = response;
	}
	
	public Object call() throws Exception {
		
        RequestContext.currentContext().unset();
        RequestContext gateContext = RequestContext.currentContext();
		try{
			
			service(request,response);
			
		}catch(Throwable t){
			logger.warn("GateCallback execute error!", t);
		}finally{
			gateContext.unset();
		}
		
		return null;
	}
	
	
	public void service(GateRequest request,GateResponse response){
		
	}
	

}
