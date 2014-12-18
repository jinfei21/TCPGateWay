package com.ctrip.gateway.route.post;

import static com.ctrip.gateway.common.Constant.*

import com.ctrip.gateway.common.GateFilter
import com.ctrip.gateway.common.GateResponse
import com.ctrip.gateway.common.RequestContext

public class SendFilter extends GateFilter{

	@Override
	public String filterType() {
		return POST;
	}

	@Override
	public int filterOrder() {
		return 30;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		
		RequestContext context = RequestContext.currentContext();
		GateResponse response = context.getGateResponse();
		
		if(response != null){
			try {
				context.set("writeResponseFuture", context.getOriginResponse().write(gateResponse));
			} catch (Throwable e) {
				logger.error("Send response error", e)
			}
		}
		return null;
	}

}
