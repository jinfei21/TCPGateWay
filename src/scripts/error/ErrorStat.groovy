package scripts.error

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.yjfei.pgateway.common.GateException
import com.yjfei.pgateway.common.GateHeaders
import com.yjfei.pgateway.context.RequestContext
import com.yjfei.pgateway.filters.GateFilter
import com.yjfei.pgateway.monitor.MetricReporter

public class ErrorStat extends GateFilter{
	private static final DynamicBooleanProperty IS_HEALTH = DynamicPropertyFactory.getInstance().getBooleanProperty("gate.is-health", true)

	@Override
	public String filterType() {
		return "error";
	}

	@Override
	boolean shouldFilter() {
		def context = RequestContext.getCurrentContext()
		return context.getThrowable() != null && !context.errorHandled()
	}

	@Override
	public int filterOrder(){
		return 10;
	}


	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		try{
			Throwable ex = ctx.getThrowable();

			String errorCause="Gate-Error-Unknown-Cause";
			int responseStatusCode;

			if (ex instanceof GateException) {
				String cause = ex.errorCause
				if(cause!=null) errorCause = cause;
				responseStatusCode = ex.nStatusCode;
			}else{
				responseStatusCode = 500;
			}

			ctx.getResponse().addHeader(GateHeaders.X_GATE_ERROR_CAUSE, errorCause);

			if (responseStatusCode == 404) {
				MetricReporter.statRouteErrorStatus("ROUTE_NOT_FOUND", errorCause)
			} else {
				MetricReporter.statRouteErrorStatus(ctx.RouteName, errorCause)
			}
		}finally{
			ctx.setErrorHandled(true)
			return null;
		}
	}
}
