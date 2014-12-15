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
}
