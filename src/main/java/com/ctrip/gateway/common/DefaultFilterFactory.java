package com.ctrip.gateway.common;


public class DefaultFilterFactory implements FilterFactory {
	
	
    public GateFilter newInstance(Class clazz) throws Exception {
        return (GateFilter) clazz.newInstance();
    }
}