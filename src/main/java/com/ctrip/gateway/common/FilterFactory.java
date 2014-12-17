package com.ctrip.gateway.common;


public interface FilterFactory {
    public GateFilter newInstance(Class clazz) throws Exception;
}