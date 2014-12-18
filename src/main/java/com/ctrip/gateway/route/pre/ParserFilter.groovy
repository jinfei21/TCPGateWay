package com.ctrip.gateway.route.pre

import static com.ctrip.gateway.common.Constant.*

import com.ctrip.gateway.common.BusinessRequest;
import com.ctrip.gateway.common.GateFilter
import com.ctrip.gateway.common.GateRequest
import com.ctrip.gateway.common.HeartBeatRequest;
import com.ctrip.gateway.common.RequestContext
import com.ctrip.gateway.util.ByteUtil;
import com.netflix.config.DynamicPropertyFactory;


class ParserFilter extends GateFilter{


	@Override
	public String filterType() {	
		return PREV;
	}

	@Override
	public int filterOrder() {
		return 10;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
	
		RequestContext context = RequestContext.currentContext();
		GateRequest gateRequest = context.getOriginRequest();
		
		if(gateRequest instanceof BusinessRequest){
			byte[] data = gateRequest.getDataBytes();
			int serviceCode = ByteUtil.ToInt(data,0);
			String address = DynamicPropertyFactory.getInstance().getStringProperty(serviceCode+".route", "none").get();
			context.setRouteAddress(address);
		}
		
		return null
	}

}
