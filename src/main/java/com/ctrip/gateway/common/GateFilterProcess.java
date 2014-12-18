package com.ctrip.gateway.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ctrip.gateway.common.Constant.*;

public class GateFilterProcess {

	private static final Logger logger = LoggerFactory.getLogger(GateFilterProcess.class);
	
	private static GateFilterProcess instance = null;
	
	
	private GateFilterProcess(){
		
	}
	
	public static GateFilterProcess instance(){
		if(instance == null){
			synchronized (GateFilterProcess.class) {
				if(instance == null){
					instance = new GateFilterProcess();
				}
			}
		}
		return instance;
	}
	
	
	public void preRoute(){
		try {
			runFilter(PREV);
		} catch (Throwable e) {
			logger.error("execute preRoute error!", e);
		}
	}
	
	public void procRoute(){
		try {
			runFilter(PROC);
		} catch (Throwable e) {
			logger.error("execute procRoute error!", e);
		}
	}
	
	public void postRoute(){
		try {
			runFilter(POST);
		} catch (Throwable e) {
			logger.error("execute postRoute error!", e);
		}
	}
	
	private Object runFilter(String type) throws Throwable{
        boolean bResult = false;
        List<GateFilter> list = GateFilterManage.instance().getGateFilterByType(type);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                GateFilter gateFilter = list.get(i);
                Object result = processGateFilter(gateFilter);
                if (result != null && result instanceof Boolean) {
                    bResult |= ((Boolean) result);
                }
            }
        }
        return bResult;
	}

	public Object processGateFilter(GateFilter filter) throws Throwable {
		String filterName = "none";
		RequestContext ctx = RequestContext.currentContext();
		Throwable t = null;
		Object o = null;
		try {
			long ltime = System.currentTimeMillis();
			filterName = filter.getClass().getSimpleName();
			GateFilterResult result = filter.runFilter();
			long execTime = System.currentTimeMillis() - ltime;
			ExecuteStatus s = result.getStatus();

			switch (s) {
				case FAILED:
					t = result.getException();
					ctx.addFilterExecutionSummary(filterName,ExecuteStatus.FAILED.name(), execTime);
					break;
				case SUCCESS:
					o = result.getResult();
					ctx.addFilterExecutionSummary(filterName,ExecuteStatus.SUCCESS.name(), execTime);
					break;
				default:
					break;
			}

			if (t != null) throw t;
			
		} catch (Throwable e) {
			logger.error("filter:" + filterName + " process error!", e);
		}
		return o;
	}
}
