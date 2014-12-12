package com.ctrip.gateway.common;

public abstract class GateFilter implements Comparable<GateFilter>{

	abstract public String filterType();
	
    abstract public int filterOrder();
    
    abstract public boolean shouldFilter();
    
    abstract public Object run();
	
}
