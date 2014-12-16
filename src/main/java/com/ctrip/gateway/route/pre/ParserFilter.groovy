package com.ctrip.gateway.route.pre

import com.ctrip.gateway.common.GateFilter;
import static com.ctrip.gateway.common.Constant.*;

class ParserFilter extends GateFilter{

	public int compareTo(GateFilter o) {

		return 0
	}

	@Override
	public String filterType() {
		
		return PREV;
	}

	@Override
	public int filterOrder() {
		return 0
	}

	@Override
	public boolean shouldFilter() {
		
		return false
	}

	@Override
	public Object run() {
	
		return null
	}

}
