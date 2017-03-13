package scripts.pre

import javax.servlet.http.HttpServletResponse

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.yjfei.pgateway.context.RequestContext
import com.yjfei.pgateway.filters.GateFilter

public class SearchRoute extends GateFilter{
	private static final DynamicBooleanProperty IS_HEALTH = DynamicPropertyFactory.getInstance().getBooleanProperty("gate.is-health", true)
	
	@Override
	public String filterType() {
		return "pre";
	}
	
	@Override
	boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext()

         if(!ctx.sendGateResponse())return false;
         if(ctx.getRouteUrl()!=null) return false;
		 
		 return true;
	}
	
	public int filterOrder(){
		return 20;
		
	}
	
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext()
		ctx.setRouteUrl("http://127.0.0.1:8080/admin/filterLoader.jsp");
		return;
	}
}
