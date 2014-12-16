package com.ctrip.gateway.common;



public class GateFilterResult {
    private Object result;
    private Throwable exception;
    private ExecuteStatus status;
    
    
    public GateFilterResult(Object result,ExecuteStatus status){
    	this.result = result;
    	this.status = status;
    }
    
    public GateFilterResult(ExecuteStatus status){
    	this.status = status;
    }
    
    public GateFilterResult() {
        this.status = ExecuteStatus.DISABLED;
    }

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteStatus status) {
		this.status = status;
	}
    
    
}
