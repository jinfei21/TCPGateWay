package com.ctrip.gateway.common;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestContext extends ConcurrentHashMap<String, Object>{

	private static final Logger logger = LoggerFactory.getLogger(RequestContext.class);
	protected static Class<? extends RequestContext> contextClazz = RequestContext.class;
	
	protected static final ThreadLocal<? extends RequestContext> threadLocal = new ThreadLocal<RequestContext>(){
        @Override
        protected RequestContext initialValue() {
            try {
                return contextClazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
	};
	
	
	public RequestContext(){
		super();
	}
	
	public static void setRequestContext(Class<? extends RequestContext> clazz){
		contextClazz = clazz;
	}
	
	public static RequestContext currentContext(){
		RequestContext context = threadLocal.get();
		return context;
	}
	
	public static void unset(){
		threadLocal.remove();
	}
	
    public void addFilterExecutionSummary(String name, String status, long time) {
        StringBuilder sb = getFilterExecutionSummary();
        if (sb.length() > 0) sb.append(", ");
        sb.append(name).append('[').append(status).append(']').append('[').append(time).append("ms]");
    }

    public StringBuilder getFilterExecutionSummary() {
        if (get("executedFilters") == null) {
            putIfAbsent("executedFilters", new StringBuilder());
        }
        return (StringBuilder) get("executedFilters");
    }
    

    public GateRequest getOriginRequest() {
        return (GateRequest) get("originRequest");
    }

    public void setOriginRequest(GateRequest request) {
        put("originRequest", request);
    }


    public GateResponse getOriginResponse() {
        return (GateResponse) get("originResponse");
    }


    public void setOriginResponse(GateResponse response) {
        set("originResponse", response);
    }
    
    public GateResponse getGateResponse() {
        return (GateResponse) get("gateResponse");
    }

    public void setGateResponse(GateResponse response) {
        set("gateResponse", response);
    }
    
    public void setRouteAddress(String address){
    	 set("routeAddress", address);
    }
    
    public String getRouteAddress(){
    	return String.valueOf(get("routeAddress"));
    }
    
    public void set(String key, Object value) {
        if (value != null) put(key, value);
        else remove(key);
    }
}
