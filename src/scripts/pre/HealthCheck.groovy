package scripts.pre

import javax.servlet.http.HttpServletResponse

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.yjfei.pgateway.context.RequestContext
import com.yjfei.pgateway.filters.GateFilter

public class HealthCheck extends GateFilter{
	private static final DynamicBooleanProperty IS_HEALTH = DynamicPropertyFactory.getInstance().getBooleanProperty("gate.is-health", true)
	
	@Override
	public String filterType() {
		return "pre";
	}
	
	public Object uri() {
		return "/pgate/domaininfo/OnService.html";
	}
	
	@Override
	boolean shouldFilter() {
		String path = RequestContext.currentContext.getRequest().getRequestURI()
		return path.equalsIgnoreCase(uri());
	}
	
	public int filterOrder(){
		return 10;
	}
	
	public String responseBody() {
		if (IS_HEALTH.get()) {
			return "4008206666";
		}else{
			return "";
		}
	}
	
	@Override
	Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		// Set the default response code for static filters to be 200
		ctx.getResponse().setStatus(HttpServletResponse.SC_OK);
		// first StaticResponseFilter instance to match wins, others do not set body and/or status
		if (ctx.getResponseBody() == null) {
			ctx.setResponseBody(responseBody())
			ctx.sendGateResponse = false;
		}
	}
}
