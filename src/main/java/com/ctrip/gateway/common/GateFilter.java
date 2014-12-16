package com.ctrip.gateway.common;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

public abstract class GateFilter implements Comparable<GateFilter>{
    private final DynamicBooleanProperty filterDisabled = DynamicPropertyFactory.getInstance().getBooleanProperty(disablePropertyName(), false);


	
    public String disablePropertyName() {
        return "gate." + this.getClass().getSimpleName() + "." + filterType() + ".disable";
    }
    
    
    public int compareTo(GateFilter filter) {
        return this.filterOrder() - filter.filterOrder();
    }
    
    public GateFilterResult runFilter() {
        GateFilterResult tr = new GateFilterResult();
        if (!filterDisabled.get()) {
            if (shouldFilter()) {
                try {
                    Object res = run();
                    tr = new GateFilterResult(res, ExecuteStatus.SUCCESS);
                } catch (Throwable e) {
                    tr = new GateFilterResult(ExecuteStatus.FAILED);
                    tr.setException(e);
                } finally {
                }
            } else {
                tr = new GateFilterResult(ExecuteStatus.SKIPPED);
            }
        }
        return tr;
    }
    
	abstract public String filterType();
	
    abstract public int filterOrder();
    
    abstract public boolean shouldFilter();
    
    abstract public Object run();
}
